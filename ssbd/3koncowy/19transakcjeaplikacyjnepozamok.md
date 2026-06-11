## 19 Identyfikacja transakcji aplikacyjnych w pozostałych modułach 3 pkt

[//]: # (Dla każdego przypadku użycia _w ramach modułów innych niż MOK_ należy przedstawić wszystkie transakcje aplikacyjne z udziałem metod komponentów aplikacji sieciowej.)

[//]: # (Zawartość rozdziału należy utworzyć zachowując wszystkie zasady opisane w analogicznym rozdziale projektu szczegółowego.)

[//]: # (Dodatkowo wymagane jest zaprezentowanie mechanizmu ponawiania odwołanych transakcji aplikacyjnych także w przypadku, gdy odwołanie transakcji nastąpiło na skutek zdarzenia innego niż wystąpienie wyjątku. W ramach prezentacji rozwiązania należy zacytować fragment kodu wybranego *przykładowego* komponentu obrazujący zastosowanie rozwiązania.)

[//]: # (_Realizacja: Wszystkie metody biznesowe wszystkich komponentów *we wszystkich modułach funkcjonalnych aplikacji* muszą być opatrzone stosownymi adnotacjami
konfigurującymi propagację transakcji aplikacyjnych.
Przedstawione rozwiązanie ustalania granic transakcji aplikacyjnych w strategii CMT i zastosowania mechanizmu blokad optymistycznych musi być zaimplementowane i
zastosowane do wszystkich klas / metod komponentów biorących udział w realizacji transakcji._)

W module MPP zarządzanie transakcjami realizowane jest w oparciu o strategię CMT z wykorzystaniem adnotacji `@Transactional`. 
Metody warstwy biznesowej (tzn. serwisy) rozpoczynają prawie zawsze nowe transakcje aplikacyjne przy użyciu propagacji `REQUIRES_NEW`.

W celu uniknięcia redundancji kodu oraz zapewnienia spójności architektonicznej wszystkie komponenty dostępowe (tzn. repozytoria) 
w module MOK posiadają adnotację na poziomie klasy, która narzuca propagację `MANDATORY`dla wszystkich metod modyfikujących 
i odczytujących stan bazy danych. Metody te nie mogą samodzielnie otworzyć nowej transakcji i muszą dołączyć do transakcji 
nadrzędnej.

### MPP.1 - Synchronizacja przedmiotu z Gitea

Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna. 
Jest ona odpowiedzialna za komunikację z instancją platformy Gitea w celu pobrania aktualnej listy repozytoriów powiązanych z organizacją przypisaną do wybranego przedmiotu, zweryfikowanie ich współtwórców oraz zaktualizowanie powiązań i stanu encji w bazie danych aplikacji.

```java
@MethodCallLogged
@Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
public void syncSubjectWithGitea(UUID subjectId) {
    Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(SubjectNotFoundException::new);

    checkIfNotArchived(subject);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    boolean isOwnerOrEditor = subject.getSubjectAssignmentList().stream()
            .anyMatch(a -> a.getTeacher().getAccount().getLogin().equals(currentUsername) &&
                    (a.getSubjectRole() == TeacherSubjectRole.OWNER || a.getSubjectRole() == TeacherSubjectRole.EDITOR));

    if (!isOwnerOrEditor) {
        throw new SubjectAccessDeniedException();
    }

    internalSyncWithGitea(subject);
    subjectRepository.saveAndFlush(subject);
}
```
Listing 19.1.1 Metoda synchronizująca przedmioty z platformą Gitea i inicjująca transakcję aplikacyjną dla przypadku MPP.1.

Transakcja jest inicjowana przez metodę `syncSubjectWithGitea(UUID)` z komponentu `SubjectService`, przedstawioną na listingu 19.1.1 W ramach tej transakcji wywoływana jest sekwencja metod komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są nimi kolejno:

- `SubjectRepository.findById(UUID)`
- `StudentRepository.findByAccountEmailIgnoreCase(String)` – wywoływana wielokrotnie w ramach metody pomocniczej `internalSyncWithGitea` w celu odszukania kont studenckich na podstawie adresów e-mail współtwórców pobranych z platformy Gitea,
- `SubjectRepository.saveAndFlush(Subject)`

Operacje te wykonywane se w granicach transakcji na następujących obiektach encji JPA:
- Subject
- Repository
- Student


### MPP.2 - Generowanie raportu na żądanie
W tym przypadku użycia zastosowano trzy transakcje aplikacyjne.

Na głównej metodzie serwisu zastosowano poziom propagacji `NOT_SUPPORTED`, czyli ona nie rozpoczyna transakcji. 

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = false, transactionManager = "mppTransactionManager")  
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")  
public Report generateReportOnDemand(UUID subjectId, String tag, String login) {  
    SubjectConfigDTO config = reportDatabaseService.verifyAccessAndGetConfig(subjectId, login);  
  
    List<GiteaRepoDTO> giteaRepoDTO = giteaService.getRepositoriesFromOrganization(config.organizationName());  
  
    long successfulClonesCount = giteaRepoDTO.stream()  
            .filter(repo -> !repo.template())  
            .map(repo -> {  
                File targetDir = new File("repos/submissions/" + config.organizationName() + "/" + tag + "/" + repo.name());  
                return GitService.cloneTag(repo.clone_url(), tag, targetDir, giteaToken);  
            })  
            .filter(success -> success)  
            .count();  
  
    if (successfulClonesCount == 0) {  
        throw new GitCloneException();  
    }  
  
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));  
    String reportPath = "reports/" + config.organizationName() + "/" + tag + "/" + timestamp + ".jplag";  
  
    JPlagResult result = jPlagService.runPlagiarismScanOnOrganisation(  
            config.organizationName(),  
            tag,  
            config.minimumTokensMatch(),  
            config.enableNormalization(),  
            reportPath  
    );  
  
    List<JPlagComparison> comparisons = result.getAllComparisons();  
  
    Double avg = comparisons.stream()  
            .mapToDouble(JPlagComparison::similarity)  
            .average()  
            .orElse(0.0);  
  
    Report savedReport = reportDatabaseService.saveGeneratedReport(  
            subjectId, tag, successfulClonesCount, reportPath, avg  
    );  
    subjectStatisticsService.recalculateAndUpdateStatistics(subjectId);  
  
    return savedReport;  
}
```

Listing 19.2.1 Metoda wykonująca generowanie raportów anty plagiatu.

Metoda na listingu 19.2.1  nie inicjuje transakcji z tego powodu że wykonuje żądania do innej aplikacji (gitea), pobiera repozytoria oraz wykonuje uciążliwe obliczenia anty plagiatu.

Pierwszą transakcję w tym przypadku użycia rozpoczyna funkcja `verifyAccessAndGetConfig()` która autoryzuje prowadzącego na podstawie tego czy jest on przypisany do przedmiotu, weryfikuje że przedmiot nie jest zarchiwizowany oraz buduje obiekt `SubjectConfigDTO` z wyłącznie istotnymi danymi dla dalszego przetwarzania.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")  
public SubjectConfigDTO verifyAccessAndGetConfig(UUID subjectId, String login) {  
    Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);  
    if (!subjectRepository.isTeacherAssignedToSubject(subjectId, login)) {  
        throw new SubjectAccessDeniedException();  
    }  
    if (subject.getArchived()) {  
        throw new SubjectArchivedException();  
    }  
  
    RulePreset options = subject.getRulePreset();  
  
    return new SubjectConfigDTO(  
            subject.getOrganizationName(),  
            options.getMinimumTokensMatch(),  
            options.getEnableNormalization()  
    );  
}
```

Listing 19.2.2 Metoda wykonująca generowanie raportów anty plagiatu.

Transakcja jest inicjowana przez metodę `verifyAccessAndGetConfig(UUID subjectId, String login)` z `ReportDatabaseService`, którą przedstawiono 
na listingu 19.2.2. W ramach tej transakcji wywoływana jest metoda komponentu dostępowego, która dołącza 
do niej z propagacją `MANDATORY`.

Jest to:
- `subjectRepository.findById(subjectId)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- `Subject`
- `RulePreset`

Druga transakcja realizuje utrwalanie obiektu encji Report.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")  
public Report saveGeneratedReport(UUID subjectId, String tag, long scannedCount, String reportPath, Double avgSimilarity) {  
    Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);  
    List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subjectId);  
  
    Report report = new Report();  
    report.setSubject(subject);  
    report.setTag(tag);  
    report.setIsStudentReport(false);  
    report.setScannedRepositories(scannedCount);  
    report.setPathToReport(reportPath);  
    report.setRepositoryList(repositoryList);  
    report.setAverageSimilarity(avgSimilarity);  
  
    return reportsRepository.saveAndFlush(report);  
}
```
Listing 19.2.3. Metoda inicjująca drugą transakcję aplikacyjną w ramach przypadku MPP.2.

Transakcja jest inicjowana przez metodę `saveGeneratedReport(UUID subjectId, String tag, long scannedCount, String reportPath, Double avgSimilarity)` z `ReportDatabaseService`, którą przedstawiono 
na listingu 19.2.3. W ramach tej transakcji wywoływane są metody komponentów dostępowych, które dołączają 
do niej z propagacją `MANDATORY`.

Są to:
- `subjectRepository.findById(subjectId)`
- `reportsRepository.saveAndFlush(report)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- `Subject`
- `Report`

Trzecia transakcja realizuje obliczenie agregowanej wartości średniego podobieństwa w encji przedmiotu.

```java
@Retryable(  
        retryFor = {JpaSystemException.class},  
        backoff = @Backoff(delayExpression = "${app.retry.backoff}"),  
        maxAttemptsExpression = "${app.retry.maxattempts}"  
)  
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")  
public void recalculateAndUpdateStatistics(UUID subjectId) {  
    Subject subject = subjectRepository.findById(subjectId)  
            .orElseThrow(SubjectNotFoundException::new);  
  
    Double newSubjectAggregate = reportsRepository.calculateAggregatedAverageForSubject(subjectId);  
  
    if (subject.getSubjectStatistics() == null) {  
        SubjectStatistics subjectStatistics = new SubjectStatistics();  
        subjectStatistics.setAverageSimilarity(newSubjectAggregate);  
        subjectStatistics.setSubject(subject);  
        subject.setSubjectStatistics(subjectStatistics);  
    } else {  
        subject.getSubjectStatistics().setAverageSimilarity(newSubjectAggregate);  
    }  
    subjectRepository.saveAndFlush(subject);  
}
```
Listing 19.2.4. Metoda inicjująca trzecią transakcję aplikacyjną w ramach przypadku MPP.2.

Transakcja jest inicjowana przez metodę `recalculateAndUpdateStatistics(UUID subjectId)` z `ReportDatabaseService`, którą przedstawiono 
na listingu 19.2.4. W ramach tej transakcji wywoływane są metody komponentów dostępowych, które dołączają 
do niej z propagacją `MANDATORY`.

Są to:
- `subjectRepository.findById(subjectId)`
- `reportsRepository.calculateAggregatedAverageForSubject(subjectId)`
- `subjectRepository.saveAndFlush(subject)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- `Subject`
- `Report`

### MPP.3 Wyświetl listę raportów
W tym przypadku użycia zastosowano jedną transakcję o charakterze wyłącznie odczytowym.

Dana transakcja realizuje odczyt wszystkich raportów do których dany prowadzący ma dostęp.

```java
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")  
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")  
public Page<Report> getAllReportsForTeacher(String login, Pageable pageable) {  
    return reportsRepository.findAllReportsForAssignedTeacherByLogin(login, pageable);  
}
```
19.3.1 . Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.3.

Transakcja jest inicjowana przez metodę `getAllReportsForTeacher(String login, Pageable pageable)` z klasy `ReportService`, którą przedstawiono 
na listingu 19.3.1. W ramach tej transakcji wywoływana jest metoda komponentu dostępowego, która dołącza do niej z propagacją `MANDATORY`.

W ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją MANDATORY:
* `reportsRepository.findAllReportsForAssignedTeacherByLogin(login, pageable)`

Operacja ta wykonywana jest na encji JPA **Report**.

### MPP.4 Przeglądanie jednego raportu

W tym przypadku użycia zastosowano jedną transakcję o charakterze wyłącznie odczytowym.

Dana transakcja realizuje odczyt szczegółowych danych wybranego raportu antyplagiatowego oraz weryfikację uprawnień zalogowanego użytkownika (Prowadzącego będącego twórcą przedmiotu lub Prowadzącego dodatkowego przypisanego do przedmiotu).

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public TeacherReportDetailsDTO getTeacherReportDetails(UUID reportId, String login) {

        Report report = reportsRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        boolean isCreator = report.getSubject().getCreatedBy().getLogin().equals(login);
        boolean isAssigned = report.getSubject().getSubjectAssignmentList().stream()
                .anyMatch(sa -> sa.getTeacher().getAccount().getLogin().equals(login));

        if (!isCreator && !isAssigned) {
            throw new SubjectAccessDeniedException();
        }

        List<TeacherComparisonDTO> parsedComparisons = JplagReportParser.parseFullTeacherReport(report.getPathToReport());

        List<TeacherComparisonDTO> sortedComparisons = parsedComparisons.stream()
                .sorted((c1, c2) -> Double.compare(
                        c2.maxSimilarity() != null ? c2.maxSimilarity() : 0.0,
                        c1.maxSimilarity() != null ? c1.maxSimilarity() : 0.0
                ))
                .toList();

        return new TeacherReportDetailsDTO(
                report.getId(),
                report.getTag(),
                report.getSubject().getName(),
                report.getCreatedAt(),
                report.getAverageSimilarity(),
                sortedComparisons
        );
    }
```
Listing 19.4.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.4.

Transakcja jest inicjowana przez metodę `getTeacherReportDetails(UUID reportId, String login)` z klasy `ReportService`, którą przedstawiono na listingu 19.4.1. W ramach tej transakcji wywoływana jest metoda komponentu dostępowego, która dołącza do niej z propagacją `MANDATORY`.

W ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją `MANDATORY`:

* `reportsRepository.findById(reportId)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
* `Report`
* `Subject`
* `SubjectAssignment`
* `Teacher`
* `Account`

### MPP.5 Usuwanie raportów.
W tym przypadku użycia zastosowano dwie transakcje.

Pierwsza transakcja realizuje usuwanie konkretnego raportu.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")  
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")  
public void deleteReport(UUID reportId) {  
    Report report = reportsRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);  
    Path filePath = Path.of(report.getPathToReport());  
  
    if (report.getSubject().getArchived()) {  
        throw new SubjectArchivedException();  
    }  
  
    try {  
        boolean deleted = Files.deleteIfExists(filePath);  
        if (!deleted) {  
            log.warn("File was not found on disk, but proceeding to delete DB record: {}", filePath);  
        }  
    } catch (IOException e) {  
        throw new DeleteReportException(e);  
    }  
  
    Subject subject = subjectRepository.findByReportId(reportId).orElseThrow(SubjectNotFoundException::new);  
  
    reportsRepository.delete(report);  
  
    subjectStatisticsService.recalculateAndUpdateStatistics(subject.getId());  
}
```
Listing 19.5.1. Metoda inicjująca pierwszą transakcję aplikacyjną w ramach przypadku MPP.5.

Transakcja jest inicjowana przez metodę `getAllReportsForTeacher(String login, Pageable pageable)` z klasy `ReportService`, którą przedstawiono 
na listingu 19.5.1. W ramach tej transakcji wywoływana jest metoda komponentu dostępowego, która dołącza do niej z propagacją `MANDATORY`.

W ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją MANDATORY:
* `reportsRepository.findById(reportId)`
* `subjectRepository.findByReportId(reportId)`
* `reportsRepository.delete(report)`

Druga transakcja realizuje obliczenie agregowanej wartości średniego podobieństwa w encji przedmiotu.

```java
@Retryable(  
        retryFor = {JpaSystemException.class},  
        backoff = @Backoff(delayExpression = "${app.retry.backoff}"),  
        maxAttemptsExpression = "${app.retry.maxattempts}"  
)  
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")  
public void recalculateAndUpdateStatistics(UUID subjectId) {  
    Subject subject = subjectRepository.findById(subjectId)  
            .orElseThrow(SubjectNotFoundException::new);  
  
    Double newSubjectAggregate = reportsRepository.calculateAggregatedAverageForSubject(subjectId);  
  
    if (subject.getSubjectStatistics() == null) {  
        SubjectStatistics subjectStatistics = new SubjectStatistics();  
        subjectStatistics.setAverageSimilarity(newSubjectAggregate);  
        subjectStatistics.setSubject(subject);  
        subject.setSubjectStatistics(subjectStatistics);  
    } else {  
        subject.getSubjectStatistics().setAverageSimilarity(newSubjectAggregate);  
    }  
    subjectRepository.saveAndFlush(subject);  
}
```
Listing 19.5.2. Metoda inicjująca trzecią transakcję aplikacyjną w ramach przypadku MPP.5.

Transakcja jest inicjowana przez metodę `recalculateAndUpdateStatistics(UUID subjectId)` z `ReportDatabaseService`, którą przedstawiono 
na listingu 19.5.2. W ramach tej transakcji wywoływane są metody komponentów dostępowych, które dołączają 
do niej z propagacją `MANDATORY`.

Są to:
- `subjectRepository.findById(subjectId)`
- `reportsRepository.calculateAggregatedAverageForSubject(subjectId)`
- `subjectRepository.saveAndFlush(subject)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- `Subject`
- `Report`

### MPP. 6 Przeglądanie przedmiotów

Na powyższy przypadek użycia składają się trzy transakcje aplikacyjne o charakterze wyłącznie odczytowym. 

Pierwsza z nich realizuje odczyt listy przedmiotów, do których przypisany jest dany prowadzący, druga odpowiada za odczyt listy przedmiotów powiązanych z zalogowanym studentem, a trzecia pobiera wszystkie przedmioty w systemie na potrzeby widoku administratora.

```java
    @MethodCallLogged
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
public List<SubjectDTO> getTeacherSubjects() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    String login = authentication.getName();
    List<SubjectDTO> result = new ArrayList<>();

    List<SubjectAssignment> assignments = subjectAssignmentRepository.findAllByTeacherLogin(login);
    List<UUID> subjectIds = assignments.stream()
            .map(assignment -> assignment.getSubject().getId())
            .toList();

    List<SubjectDescriptionTranslation> translations = subjectDescriptionTranslationRepository
            .findBySubjectIdInAndIsOriginalTrue(subjectIds);

    Map<UUID, String> descriptionMap = translations.stream()
            .collect(Collectors.toMap(
                    SubjectDescriptionTranslation::getId,
                    SubjectDescriptionTranslation::getTranslatedDescription
            ));

    Map<UUID, LocalDateTime> nextScanMap = scheduleRepository.findNextScanTimesForSubjects(subjectIds)
            .stream()
            .collect(Collectors.toMap(
                    ScheduleRepository.SubjectNextScan::getSubjectId,
                    ScheduleRepository.SubjectNextScan::getNextScanTime
            ));

    for (SubjectAssignment assignment : assignments) {
        Subject subject = assignment.getSubject();
        TeacherSubjectRole role = assignment.getSubjectRole();
        String originalDescription = descriptionMap.getOrDefault(subject.getId(), "");

        boolean canEdit = (role == TeacherSubjectRole.OWNER || role == TeacherSubjectRole.EDITOR);
        boolean canManageTeachers = (role == TeacherSubjectRole.OWNER);
        String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());
        LocalDateTime nextScan = nextScanMap.get(subject.getId());

        result.add(subjectMapper.toSubjectDTO(subject, hash, canEdit, canManageTeachers, true, null, null, originalDescription, nextScan));
    }

    return result;
}
```
Listing 19.6.1. Metoda pobierająca przedmioty prowadzącego i rozpoczynająca pierwszą transakcję aplikacyjną dla przypadku MPP.6.

Transakcja jest inicjowana przez metodę `getTeacherSubjects()` z `SubjectService`, którą przedstawiono na listingu 19.6.1. W ramach tej transakcji wywoływana jest sekwencja metod komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są nimi kolejno:
- `SubjectAssignmentRepository.findAllByTeacherLogin(String)`
- `SubjectDescriptionTranslationRepository.findBySubjectIdInAndIsOriginalTrue(List<UUID>)`
- `ScheduleRepository.findNextScanTimesForSubjects(List<UUID>)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
- `SubjectAssignment`
- `Subject`
- `SubjectDescriptionTranslation`
- `Schedule`

Druga transakcja z kolei realizuje odczyt wszystkich przedmiotów, do których przypisany jest dany student.

```java
@MethodCallLogged
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
public List<SubjectDTO> getStudentSubjects() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    String login = authentication.getName();

    List<SubjectDTO> result = new ArrayList<>();
    List<Subject> subjects = subjectRepository.findAllByStudentLogin(login);

    List<UUID> subjectIds = subjects.stream()
            .map(Subject::getId)
            .toList();

    List<SubjectDescriptionTranslation> translations = subjectDescriptionTranslationRepository
            .findBySubjectIdInAndIsOriginalTrue(subjectIds);

    Map<UUID, String> descriptionMap = translations.stream()
            .collect(Collectors.toMap(
                    SubjectDescriptionTranslation::getId,
                    SubjectDescriptionTranslation::getTranslatedDescription
            ));

    Map<UUID, LocalDateTime> nextScanMap = scheduleRepository.findNextScanTimesForSubjects(subjectIds)
            .stream()
            .collect(Collectors.toMap(
                    ScheduleRepository.SubjectNextScan::getSubjectId,
                    ScheduleRepository.SubjectNextScan::getNextScanTime
            ));

    for (Subject subject : subjects) {
        String originalDescription = descriptionMap.getOrDefault(subject.getId(), "");
        String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());
        LocalDateTime nextScan = nextScanMap.get(subject.getId());

        result.add(subjectMapper.toSubjectDTO(subject, hash, false, false, false, null, null, originalDescription, nextScan));
    }

    return result;
}
```
Listing 19.6.2. Metoda inicjująca drugą transakcję aplikacyjną w ramach przypadku MPP.6 oraz pobierająca przedmioty studenta.

Druga transakcja w ramach MPP.6 jest inicjowana przez metodę `getStudentSubjects`() z `SubjectService`, przedstawioną na listingu 19.6.2. W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
- `SubjectRepository.findAllByStudentLogin(String)`
- `SubjectDescriptionTranslationRepository.findBySubjectIdInAndIsOriginalTrue(List<UUID>)`
- `ScheduleRepository.findNextScanTimesForSubjects(List<UUID>)`

Operacje te wykonywane są na encjach JPA:
- `Subject`
- `SubjectDescriptionTranslation`
- `Schedule`

```java
    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public List<SubjectDTO> getAllSubjects() {
        List<SubjectDTO> result = new ArrayList<>();
        List<Subject> subjects = subjectRepository.findAll();

        List<UUID> subjectIds = subjects.stream()
                .map(Subject::getId)
                .toList();

        List<SubjectDescriptionTranslation> translations = subjectDescriptionTranslationRepository
                .findBySubjectIdInAndIsOriginalTrue(subjectIds);

        Map<UUID, String> descriptionMap = translations.stream()
                .collect(Collectors.toMap(
                        SubjectDescriptionTranslation::getId,
                        SubjectDescriptionTranslation::getTranslatedDescription
                ));

        Map<UUID, LocalDateTime> nextScanMap = scheduleRepository.findNextScanTimesForSubjects(subjectIds)
                .stream()
                .collect(Collectors.toMap(
                        ScheduleRepository.SubjectNextScan::getSubjectId,
                        ScheduleRepository.SubjectNextScan::getNextScanTime
                ));

        for (Subject subject : subjects) {
            String originalDescription = descriptionMap.getOrDefault(subject.getId(), "");
            String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());
            LocalDateTime nextScan = nextScanMap.get(subject.getId());

            result.add(subjectMapper.toSubjectDTO(subject, hash, true, true, true, null, null, originalDescription, nextScan));
        }

        return result;
    }
```
Listing 19.6.3. Metoda pobierająca wszystkie przedmioty dla administratora i inicjująca trzecią transakcję aplikacyjną dla przypadku MPP.6.

Trzecia transakcja jest inicjowana przez metodę `getAllSubjects()` z komponentu `SubjectService`, którą przedstawiono na listingu 19.6.3. W ramach tej transakcji wywoływane są metody komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są to kolejno:
`SubjectRepository.findAll()`
`SubjectDescriptionTranslationRepository.findBySubjectIdInAndIsOriginalTrue(List<UUID>)`
`ScheduleRepository.findNextScanTimesForSubjects(List<UUID>)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
`Subject`
`SubjectDescriptionTranslation`
`Schedule`

### MPP.7 Tworzenie przedmiotów

Na powyższy przypadek użycia składają się dwie transakcje aplikacyjne.

Pierwsza z nich realizuje utworzenie przedmiotu na podstawie ręcznej konfiguracji reguł wprowadzanej przez prowadzącego.

```java
@MethodCallLogged
@Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
public void createSubjectManual(CreateSubjectManualDTO dto, String creatorLogin, String originalLanguage) {
    Teacher teacher = teacherRepository.findActiveTeacherByLogin(creatorLogin)
            .orElseThrow(AccessLevelNotAssignedException::new);

    if (subjectRepository.existsByOrganizationNameIgnoreCase(dto.organizationName())) {
        throw new SubjectAlreadyExistsException();
    }

    String generatedGiteaUrl = "https://team-1.proj-sum.it.p.lodz.pl/git/" + dto.organizationName();

    Subject subject = createBaseSubject(
            dto.name(),
            dto.organizationName(),
            dto.edition(),
            dto.subjectDescription(),
            generatedGiteaUrl,
            originalLanguage
    );

    RulePreset preset = new RulePreset();
    preset.setRaportLevel(fetchRaportLevel(dto.manualRules().raportLevelName()));
    preset.setStudentTicketCount(dto.manualRules().studentTicketCount());
    preset.setMinimumTokensMatch(dto.manualRules().minimumTokensMatch());
    preset.setEnableNormalization(dto.manualRules().enableNormalization());
    preset.setIsPreset(false);
    preset.setSubject(subject);
    subject.setRulePreset(preset);

    assignTeachers(subject, teacher, creatorLogin, dto.teachers());
    internalSyncWithGitea(subject);
//        subjectRepository.saveAndFlush(subject);
    Subject savedSubject = subjectRepository.saveAndFlush(subject);
    SubjectDescriptionTranslation subjectDescriptionTranslation = new SubjectDescriptionTranslation();
    subjectDescriptionTranslation.setTranslatedDescription(dto.subjectDescription());
    subjectDescriptionTranslation.setLanguageDict(
            languageDictRepository.findByLanguage(originalLanguage)
                    .orElseThrow(LanguageDictNotFoundException::new)
    );
    subjectDescriptionTranslation.setIsOriginal(true);
    subjectDescriptionTranslation.setSubject(savedSubject);
    savedSubject.getSubjectDescriptionTranslationList().add(subjectDescriptionTranslation);
    subjectDescriptionTranslationRepository.saveAndFlush(subjectDescriptionTranslation);
}
```
Listing 19.7.1. Metoda tworząca przedmiot z ręczną konfiguracją reguł i rozpoczynająca pierwszą transakcję aplikacyjną dla przypadku MPP.7.

Pierwsza transakcja jest inicjowana przez metodę `createSubjectManual(CreateSubjectManualDTO, String, String)` z `SubjectService`, którą przedstawiono na listingu 19.7.1. W ramach tej transakcji wywoływana jest sekwencja metod komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są nimi kolejno:
- `TeacherRepository.findActiveTeacherByLogin(String)`
- `SubjectRepository.existsByOrganizationNameIgnoreCase(String)`
- `RaportLevelRepository.findByName(String)`
- `StudentRepository.findByAccountEmailIgnoreCase(String)`
- `SubjectRepository.saveAndFlush(Subject)`
- `LanguageDictRepositoryMPP.findByLanguage(String)`
- `SubjectDescriptionTranslationRepository.saveAndFlush(SubjectDescriptionTranslation)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
- `Teacher`
- `Subject`
- `ReportLevelDict`
- `Student`
- `RulePreset`
- `SubjectAssignment`
- `Repository`
- `LanguageDict`
- `SubjectDescriptionTranslation`

Druga transakcja z kolei realizuje utworzenie przedmiotu z wykorzystaniem gotowego szablonu reguł skanowania zapisanego w systemie.

```java
@MethodCallLogged
@Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
public void createSubjectFromTemplate(CreateSubjectTemplateDTO dto, String creatorLogin, String originalLanguage) {
    Teacher teacher = teacherRepository.findActiveTeacherByLogin(creatorLogin)
            .orElseThrow(AccessLevelNotAssignedException::new);

    if (subjectRepository.existsByOrganizationNameIgnoreCase(dto.organizationName())) {
        throw new SubjectAlreadyExistsException();
    }

    String generatedGiteaUrl = "https://team-1.proj-sum.it.p.lodz.pl/git/" + dto.organizationName();

    Subject subject = createBaseSubject(
            dto.name(),
            dto.organizationName(),
            dto.edition(),
            dto.subjectDescription(),
            generatedGiteaUrl,
            originalLanguage
    );
    RulePreset template = ruleRepository.findById(dto.templateId())
            .orElseThrow(RulePresetTemplateNotFoundException::new);

    RulePreset preset = new RulePreset();
    preset.setRaportLevel(fetchRaportLevel(dto.raportLevelName()));
    preset.setStudentTicketCount(template.getStudentTicketCount());
    preset.setMinimumTokensMatch(template.getMinimumTokensMatch());
    preset.setEnableNormalization(template.getEnableNormalization());
    preset.setIsPreset(false);
    preset.setSubject(subject);
    subject.setRulePreset(preset);

    assignTeachers(subject, teacher, creatorLogin, dto.teachers());
    internalSyncWithGitea(subject);
    Subject savedSubject = subjectRepository.saveAndFlush(subject);
    SubjectDescriptionTranslation subjectDescriptionTranslation = new SubjectDescriptionTranslation();
    subjectDescriptionTranslation.setTranslatedDescription(dto.subjectDescription());
    subjectDescriptionTranslation.setLanguageDict(
            languageDictRepository.findByLanguage(originalLanguage)
                    .orElseThrow(LanguageDictNotFoundException::new)
    );
    subjectDescriptionTranslation.setIsOriginal(true);
    subjectDescriptionTranslation.setSubject(savedSubject);
    savedSubject.getSubjectDescriptionTranslationList().add(subjectDescriptionTranslation);
    subjectDescriptionTranslationRepository.saveAndFlush(subjectDescriptionTranslation);
}
```
Listing 19.7.2. Metoda tworząca przedmiot na podstawie szablonu i inicjująca drugą transakcję aplikacyjną dla przypadku MPP.7.

Druga transakcja w ramach MPP.7 jest inicjowana przez metodę `createSubjectFromTemplate(CreateSubjectTemplateDTO, String, String)` z `SubjectService`, przedstawioną na listingu 19.7.2. W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:

`TeacherRepository.findActiveTeacherByLogin(String)`
`SubjectRepository.existsByOrganizationNameIgnoreCase(String)`
`RuleRepository.findById(UUID)`
`RaportLevelRepository.findByName(String)`
`StudentRepository.findByAccountEmailIgnoreCase(String)`
`SubjectRepository.saveAndFlush(Subject)`
`LanguageDictRepositoryMPP.findByLanguage(String)`
`SubjectDescriptionTranslationRepository.saveAndFlush(SubjectDescriptionTranslation)`

Operacje te wykonywane są na encjach JPA:
`Teacher`
`Subject`
`RulePreset`
`ReportLevelDict`
`Student`
`SubjectAssignment`
`Repository`
`LanguageDict`
`SubjectDescriptionTranslation`

### MPP.8 Edycja przedmiotu

Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna. Realizuje ona modyfikację ustawień istniejącego przedmiotu (w tym jego nazwy, opisu, reguł skanowania oraz przypisanych prowadzących), z jednoczesnym wymuszeniem podbicia wersji encji na poziomie bazy danych w celu ochrony przed współbieżnymi modyfikacjami.

```java
@MethodCallLogged
@Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
public void updateSubject(UUID subjectId, UpdateSubjectDTO dto, String versionHash, String originalLanguage) {
    Subject subject = subjectRepository.findByIdForceIncrement(subjectId)
            .orElseThrow(SubjectNotFoundException::new);

    checkIfNotArchived(subject);

    if (!versionHash.equals(versionHashService.hashObjectVersion(subjectId, subject.getVersion()))) {
        throw new ConcurrentUpdateException();
    }

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    boolean isOwner = subject.getSubjectAssignmentList().stream()
            .anyMatch(a -> a.getTeacher().getAccount().getLogin().equals(currentUsername) && a.getSubjectRole() == TeacherSubjectRole.OWNER);
    boolean isEditor = subject.getSubjectAssignmentList().stream()
            .anyMatch(a -> a.getTeacher().getAccount().getLogin().equals(currentUsername) && a.getSubjectRole() == TeacherSubjectRole.EDITOR);

    if (!isOwner && !isEditor) {
        throw new SubjectAccessDeniedException();
    }

    subjectDescriptionTranslationRepository.deleteAllBySubjectId(subject.getId());

    SubjectDescriptionTranslation subjectDescriptionTranslation = new SubjectDescriptionTranslation();
    subjectDescriptionTranslation.setSubject(subject);
    subjectDescriptionTranslation.setTranslatedDescription(dto.subjectDescription());
    subjectDescriptionTranslation.setIsOriginal(true);
    LanguageDict languageDict = languageDictRepository.findByLanguage(originalLanguage)
            .orElseThrow(LanguageDictNotFoundException::new);
    subjectDescriptionTranslation.setLanguageDict(languageDict);

    subject.setName(dto.name());
    subject.getSubjectDescriptionTranslationList().add(subjectDescriptionTranslation);

    subjectDescriptionTranslationRepository.saveAndFlush(subjectDescriptionTranslation);

    if (dto.rules() != null) {
        RulePreset preset = subject.getRulePreset();
        preset.setRaportLevel(fetchRaportLevel(dto.rules().raportLevelName()));
        preset.setStudentTicketCount(dto.rules().studentTicketCount());
        preset.setMinimumTokensMatch(dto.rules().minimumTokensMatch());
        preset.setEnableNormalization(dto.rules().enableNormalization());
        preset.setIsPreset(false);
    }
    if (!isOwner && isEditor) {
        boolean isTeacherListModified = false;

        if (dto.teachers() != null) {
            if (dto.teachers().size() != subject.getSubjectAssignmentList().size()) {
                isTeacherListModified = true;
            } else {
                for (TeacherAssignmentDTO incoming : dto.teachers()) {
                    boolean match = subject.getSubjectAssignmentList().stream()
                            .anyMatch(existing -> existing.getTeacher().getAccount().getLogin().equals(incoming.login()) &&
                                    existing.getSubjectRole() == incoming.role());
                    if (!match) {
                        isTeacherListModified = true;
                        break;
                    }
                }
            }
        }

        if (isTeacherListModified) {
            throw new SubjectAccessDeniedException();
        }
    }

    if (isOwner) {
        List<SubjectAssignment> currentAssignments = subject.getSubjectAssignmentList();

        List<SubjectAssignment> assignmentsToRemove = currentAssignments.stream()
                .filter(assignment -> dto.teachers().stream()
                        .noneMatch(t -> t.login().equals(assignment.getTeacher().getAccount().getLogin())))
                .toList();

        currentAssignments.removeAll(assignmentsToRemove);
        subjectAssignmentRepository.deleteAll(assignmentsToRemove);

        for (TeacherAssignmentDTO teacherDto : dto.teachers()) {
            Optional<SubjectAssignment> existing = currentAssignments.stream()
                    .filter(a -> a.getTeacher().getAccount().getLogin().equals(teacherDto.login()))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().setSubjectRole(teacherDto.role());
            } else {
                Teacher teacher = teacherRepository.findActiveTeacherByLogin(teacherDto.login())
                        .orElseThrow(AccessLevelNotAssignedException::new);

                SubjectAssignment newAssignment = new SubjectAssignment();
                newAssignment.setSubject(subject);
                newAssignment.setTeacher(teacher);
                newAssignment.setSubjectRole(teacherDto.role());
                currentAssignments.add(newAssignment);
            }
        }

        boolean hasOwner = currentAssignments.stream()
                .anyMatch(a -> a.getSubjectRole() == TeacherSubjectRole.OWNER);
        if (!hasOwner) {
            throw new SubjectMustHaveOwnerException();
        }
    }

    subjectRepository.saveAndFlush(subject);
}
```
Listing 19.8.1. Metoda modyfikująca przedmiot i rozpoczynająca transakcję aplikacyjną dla przypadku MPP.8.

Transakcja jest inicjowana przez metodę `updateSubject(UUID, UpdateSubjectDTO, String, String)` z `SubjectService`, którą przedstawiono na listingu 19.8.1. W ramach tej transakcji wywoływana jest sekwencja metod komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są nimi kolejno:
- `SubjectRepository.findByIdForceIncrement(UUID)`
- `SubjectDescriptionTranslationRepository.deleteAllBySubjectId(UUID)`
- `LanguageDictRepositoryMPP.findByLanguage(String)`
- `SubjectDescriptionTranslationRepository.saveAndFlush(SubjectDescriptionTranslation)`
- `RaportLevelRepository.findByName(String)`
- `SubjectAssignmentRepository.deleteAll(Iterable)`
- `TeacherRepository.findActiveTeacherByLogin(String)`
- `SubjectRepository.saveAndFlush(Subject)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
- `Subject`
- `SubjectDescriptionTranslation`
- `LanguageDict`
- `ReportLevelDict`
- `SubjectAssignment`
- `Teacher`
- `RulePreset`

### MPP.9 Usuwanie przedmiotu

W tym przypadku użycia zastosowano jedną transakcję o charakterze odczytowo-zapisowym (modyfikującym).

Dana transakcja realizuje proces kaskadowego usunięcia przedmiotu z bazy danych wraz z fizycznym usunięciem powiązanych plików raportów (wykonywanym wyłącznie po zatwierdzeniu transakcji) oraz weryfikuje uprawnienia zarządzającego prowadzącego.

```java
@MethodCallLogged
@Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
public void deleteSubject(UUID subjectId, String versionHash, String currentUsername, boolean deleteGiteaOrg) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        checkIfNotArchived(subject);

        if (!versionHash.equals(versionHashService.hashObjectVersion(subjectId, subject.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        boolean hasPermission = subject.getSubjectAssignmentList().stream()
                .anyMatch(assignment ->
                        assignment.getTeacher().getAccount().getLogin().equals(currentUsername) &&
                                assignment.getSubjectRole().equals(TeacherSubjectRole.OWNER)
                );

        if (!hasPermission) {
            throw new SubjectAccessDeniedException();
        }

        List<String> reportPaths = subject.getReportList().stream()
                .map(Report::getPathToReport)
                .filter(Objects::nonNull)
                .toList();

        if (deleteGiteaOrg) {
            try {
                giteaService.deleteGiteaOrganization(subject.getOrganizationName());
            } catch (Exception e) {
                log.error("Błąd komunikacji z Gitea dla organizacji: {}", subject.getOrganizationName(), e);
                throw new GiteaIntegrationException(e);
            }
        }

        subjectRepository.delete(subject);
        subjectRepository.flush();

        postCommitTransactionManager.register(() -> {
            for (String path : reportPaths) {
                try {
                    Files.deleteIfExists(Paths.get(path));
                    log.info("Plik raportu został trwale usunięty z dysku: {}", path);
                } catch (IOException e) {
                    log.error("Nie udało się usunąć fizycznego pliku z dysku po usunięciu przedmiotu. Ścieżka: {}", path, e);
                }
            }
        });
    }
```
Listing 19.9.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.9.

Transakcja jest inicjowana przez metodę `deleteSubject(UUID subjectId, String versionHash, String currentUsername, boolean deleteGiteaOrg)` z odpowiedniej klasy serwisowej, którą przedstawiono na listingu 19.9.1. W ramach tej transakcji wywoływane są metody komponentu dostępowego, które dołączają do niej z propagacją `MANDATORY`.

W ramach tej transakcji wywoływane są metody komponentu dostępowego z propagacją `MANDATORY`:

* `subjectRepository.findById(subjectId)`
* `subjectRepository.delete(subject)`
* `subjectRepository.flush()`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:

* `Subject`
* `SubjectAssignment`
* `Teacher`
* `Account`
* `Report`

W metodzie zaimplementowano mechanizm blokady optymistycznej oraz ponawiania transakcji aplikacyjnej. Stan przedmiotu weryfikowany jest za pomocą zahaszowanej wartości wersji (versionHash). W przypadku wykrycia modyfikacji współbieżnej rzucany jest wyjątek ConcurrentUpdateException. Adnotacja @Retryable definiuje, że w przypadku wystąpienia tego wyjątku (lub błędu systemowego bazy danych JpaSystemException) system automatycznie podejmie próbę ponownego wykonania całej transakcji zgodnie z narzuconą polityką opóźnień (backoff). Zastosowano także rozwiązanie wykorzystujące postCommitTransactionManager, które upewnia się, że nieodwracalne usunięcie plików z pamięci masowej serwera wykona się dopiero po zatwierdzeniu głównej transakcji w bazie danych.

### MPP.10 Przeglądanie reguł

W tym przypadku użycia zastosowano jedną transakcję o charakterze wyłącznie odczytowym.

Dana transakcja realizuje odczyt wszystkich globalnych szablonów reguł wraz z obliczeniem hasha wersji każdego z nich.

```java
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
public List getRulePresetsTemplates() {
    return ruleRepository.findAllByIsPreset(true).stream()
            .map(preset -> {
                RulePresetDTO dto = ruleMapper.toRuleDTO(preset);
                return new RulePresetDTO(
                        dto.id(),
                        dto.raportLevelName(),
                        dto.studentTicketCount(),
                        dto.minimumTokensMatch(),
                        dto.enableNormalization(),
                        dto.version(),
                        versionHashService.hashObjectVersion(preset.getId(), preset.getVersion())
                );
            })
            .collect(Collectors.toList());
}
```

Listing 19.10.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.10.

Transakcja jest inicjowana przez metodę `getRulePresetsTemplates()` z klasy `RuleService`, którą przedstawiono
na listingu 19.10.1. W ramach tej transakcji wywoływana jest metoda komponentu dostępowego, która dołącza do niej z propagacją `MANDATORY`.

Jest to ta metoda:
* `ruleRepository.findAllByIsPreset(true)`

Operacja ta wykonywana jest na encji JPA **RulePreset**.

### MPP.11 Tworzenie reguł

W tym przypadku użycia zastosowano jedną transakcję o charakterze zapisu.

Dana transakcja realizuje utworzenie nowego globalnego szablonu reguł na podstawie przekazanych danych oraz powiązanie go z odpowiednim poziomem raportu ze słownika.

```java
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
public void createRulePreset(CreateRulePresetDTO dto) {
    RulePreset newPreset = new RulePreset();
    newPreset.setIsPreset(true);
    newPreset.setSubject(null);

    if (dto.raportLevelName() == null || dto.studentTicketCount() == null ||
            dto.minimumTokensMatch() == null || dto.enableNormalization() == null) {
        throw new RulePresetInvalidPartametersException();
    }

    ReportLevelDict raportLevel = raportLevelDictRepository.findByName(dto.raportLevelName())
            .orElseThrow(RaportLevelDictNotFoundException::new);

    newPreset.setRaportLevel(raportLevel);
    newPreset.setStudentTicketCount(dto.studentTicketCount());
    newPreset.setMinimumTokensMatch(dto.minimumTokensMatch());
    newPreset.setEnableNormalization(dto.enableNormalization());

    ruleRepository.save(newPreset);
}
```

Listing 19.11.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.11.

Transakcja jest inicjowana przez metodę `createRulePreset(CreateRulePresetDTO dto)` z klasy `RuleService`, którą przedstawiono
na listingu 19.11.1. W ramach tej transakcji wywoływane są metody komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są to:
- `raportLevelDictRepository.findByName(dto.raportLevelName())`
- `ruleRepository.save(newPreset)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- `ReportLevelDict`
- `RulePreset`

### MPP.12 Edycja reguł

W tym przypadku użycia zastosowano jedną transakcję o charakterze zapisu z zastosowaniem mechanizmu blokad optymistycznych.

Dana transakcja realizuje modyfikację istniejącego szablonu reguł. Przed zapisem weryfikowana jest spójność wersji obiektu przy pomocy hasha, co zabezpiecza przed równoczesną modyfikacją przez wielu użytkowników.

```java
@MethodCallLogged
@Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
public void updateRulePreset(UUID ruleId, UpdateRulePresetDTO ruleDto, String versionHash) {
    RulePreset rulePreset = ruleRepository.findById(ruleId)
            .orElseThrow(RulePresetNotFoundException::new);

    if (!versionHash.equals(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion()))) {
        throw new ConcurrentUpdateException();
    }

    ReportLevelDict raportLevel = raportLevelDictRepository.findByName(ruleDto.raportLevelName())
            .orElseThrow(RaportLevelDictNotFoundException::new);

    rulePreset.setStudentTicketCount(ruleDto.studentTicketCount());
    rulePreset.setMinimumTokensMatch(ruleDto.minimumTokensMatch());
    rulePreset.setEnableNormalization(ruleDto.enableNormalization());
    rulePreset.setRaportLevel(raportLevel);

    ruleRepository.saveAndFlush(rulePreset);
}
```

Listing 19.12.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.12.

Transakcja jest inicjowana przez metodę `updateRulePreset(UUID ruleId, UpdateRulePresetDTO ruleDto, String versionHash)` z klasy `RuleService`, którą przedstawiono
na listingu 19.12.1. W ramach tej transakcji wywoływane są metody komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są to:
- `ruleRepository.findById(ruleId)`
- `raportLevelDictRepository.findByName(ruleDto.raportLevelName())`
- `ruleRepository.saveAndFlush(rulePreset)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- `RulePreset`
- `ReportLevelDict`

### MPP 13: Usuwanie reguł

W tym przypadku użycia zastosowano jedną transakcję o charakterze odczytowo-zapisowym (modyfikującym).

Dana transakcja realizuje proces trwałego usunięcia z bazy danych zestawu reguł (presetu) powiązanego z przedmiotem. Ponieważ encja ta jest nadrzędna w stosunku do harmonogramów sprawdzania, operacja ta wymusza również kaskadowe usunięcie powiązanych obiektów podrzędnych.

```java
@MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public void deleteRulePreset(UUID ruleId, String versionHash) {

        RulePreset rulePreset = ruleRepository.findById(ruleId)
                .orElseThrow(RulePresetNotFoundException::new);

        if (!versionHash.equals(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        if (!Boolean.TRUE.equals(rulePreset.getIsPreset())) {
            throw new RulePresetNoAccessException();
        }

        ruleRepository.delete(rulePreset);
        ruleRepository.flush();
    }
```
Listing 19.13.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.13.

Transakcja jest inicjowana przez metodę `deleteRulePreset(UUID ruleId, String versionHash)` z odpowiedniej klasy serwisowej, którą przedstawiono na listingu 19.13.1. W ramach tej transakcji wywoływane są metody komponentu dostępowego, które dołączają do niej z propagacją `MANDATORY`.

W ramach tej transakcji wywoływane są metody komponentu dostępowego z propagacją `MANDATORY`:

* `ruleRepository.findById(ruleId)`
* `ruleRepository.delete(rulePreset)`
* `ruleRepository.flush()`

Operacja ta wykonywana jest w granicach transakcji na następującym obiekcie encji JPA (oraz kaskadowo na powiązanych z nim obiektach podrzędnych):

* `RulePreset`
* `Schedule` (usuwane kaskadowo)

W tej metodzie zastosowano mechanizm blokady optymistycznej oraz ponawiania odwołanych transakcji. Integralność danych weryfikowana jest za pomocą porównania zahaszowanej wartości wersji przysłanej z frontu (versionHash) z aktualną wersją w bazie. Niezgodność skutkuje wyrzuceniem wyjątku ConcurrentUpdateException. Adnotacja @Retryable definiuje, że w przypadku wystąpienia tego błędu (związanego ze współbieżną modyfikacją) lub błędu infrastrukturalnego bazy (JpaSystemException), aplikacja automatycznie, na poziomie serwisu, ponowi próbę wykonania całej transakcji zgodnie ze zdefiniowaną polityką prób (maxAttemptsExpression) i opóźnień (backoff).

### MPP.14 Przeglądanie harmonogramu
Na powyższy przypadek użycia składa się jedna transakcja odczytowa.

Dana transakcja realizuje odczyt listy harmonogramów powiązanych z danym przedmiotem wraz z weryfikacją uprawnień 
zalogowanego prowadzącego do tego przedmiotu.

```java //todo zmienić
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
public List<ScheduleDTO> getSchedules(UUID subjectId) {
    String teacherLogin = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    Optional<SubjectAssignment> subjectAssignment = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(teacherLogin, subjectId);

    if (subjectAssignment.isEmpty()) {
        throw new NoPermissionForThisSubjectException();
    }

    return scheduleRepository.findAllBySubjectId(subjectId).stream()
            .map(schedule -> {
                ScheduleDTO dto = scheduleMapper.toScheduleDTO(schedule);
                return new ScheduleDTO(
                        dto.id(),
                        dto.scheduleDateTime(),
                        dto.tag(),
                        versionHashService.hashObjectVersion(schedule.getId(), schedule.getVersion()),
                        dto.isExecuted(),
                        dto.hasFailed()
                );
            })
            .toList();
}
```
Listing 19.14.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.14.

Transakcja jest inicjowana przez metodę `getSchedules(UUID subjectId)` z klasy `ScheduleService`, którą przedstawiono 
na listingu 19.14.1. W ramach tej transakcji wywoływane są metody komponentów dostępowych, które dołączają do niej 
z propagacją `MANDATORY`.

Są to:
* `SubjectAssignmentRepository.findByTeacherLoginAndSubjectId(String, UUID)`
* `ScheduleRepository.findAllBySubjectId(UUID)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
* `SubjectAssignment`
* `Schedule`

### MPP.15 Tworzenie harmonogramu
Powyższy przypadek użycia sklada się z jednej transakcji aplikacyjnej.

Dana transakcja realizuje utworzenie nowego harmonogramu automatycznego skanowania antyplagiatowego dla danego przedmiotu, 
po uprzedniej weryfikacji uprawnień prowadzącego oraz sprawdzeniu, czy przedmiot posiada przypisany zestaw reguł.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public ScheduleDTO createSchedule(UUID subjectId, Schedule schedule, String teacherLogin) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);

        checkIfNotArchived(subject);

        if (!subjectAssignmentRepository.hasEditPermissions(subject, teacherLogin)) {
            throw new NoPermissionForThisSubjectException();
        }

        if (subject.getRulePreset() == null) {
            throw new RulePresetNotSetForSubjectException("error.rulePresetNotSetForSubject");
        }

        schedule.setRulePreset(subject.getRulePreset());
        schedule.setModifiableUntil(schedule.getScheduleDateTime());
        Schedule savedSchedule = scheduleRepository.saveAndFlush(schedule);

        ScheduleDTO dto = scheduleMapper.toScheduleDTO(savedSchedule);

        return new ScheduleDTO(
                dto.id(),
                dto.scheduleDateTime(),
                dto.tag(),
                versionHashService.hashObjectVersion(savedSchedule.getId(), savedSchedule.getVersion()),
                dto.isExecuted(),
                dto.hasFailed()
        );
    }
```
Listing 19.15.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.15.

Transakcja jest inicjowana przez metodę `createSchedule(UUID subjectId, Schedule schedule, String teacherLogin)` 
z klasy `ScheduleService`, którą przedstawiono na listingu 19.15.1. W ramach tej transakcji wywoływane są metody 
komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są to:
* `SubjectRepository.findById(UUID)`
* `SubjectAssignmentRepository.hasEditPermissions(Subject, String)`
* `ScheduleRepository.saveAndFlush(Schedule)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
* `Subject`
* `SubjectAssignment`
* `RulePreset`
* `Schedule`

### MPP.16 Edycja harmonogramu

W tym przypadku użycia zastosowano jedną transakcję o charakterze zapisu z zastosowaniem mechanizmu blokad optymistycznych.

Dana transakcja realizuje edycję harmonogramu skanowania repozytoriów. Przed zapisem sprawdzane są uprawnienia prowadzącego do możliwego zapisu oraz hash chroniący przed współbieżnym zapisem.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void updateSchedule(UUID scheduleId, UpdateScheduleDTO scheduleDto, String userLogin) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(ScheduleNotFoundException::new);

        if (!scheduleDto.getVersionHash().equals(versionHashService.hashObjectVersion(scheduleId, schedule.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        Subject subject = schedule.getRulePreset().getSubject();
        boolean hasPermission = subjectAssignmentRepository.hasEditPermissions(subject, userLogin);

        checkIfNotArchived(subject);

        if (!hasPermission) {
            throw new NoPermissionToEditScheduleException();
        }

        schedule.setScheduleDateTime(scheduleDto.getScheduleDateTime());
        schedule.setTag(scheduleDto.getTag());

        scheduleRepository.save(schedule);
    }
```

Listing 19.16.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.16.

Transakcja jest inicjowana przez metodę updateSchedule(UUID scheduleId, UpdateScheduleDTO scheduleDto, String userLogin) z klasy ScheduleService, którą przedstawiono na listingu 19.16.1. W ramach tej transakcji wywoływane są metody komponentów z propagacją REQUIRES_NEW.

Są to metody:
- scheduleRepository.findById(scheduleId)
- subjectAssignmentRepository.hasEditPermissions(subject, userLogin)
- scheduleRepository.save(schedule)

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- Schedule,
- Subject,
- RulePreset,
- SubjectAssignment.

### MPP.17 Usuwanie harmonogramu

W tym przypadku użycia zastosowano jedną transakcję o charakterze zapisu z zastosowaniem mechanizmu blokad optymistycznych.

Dana transakcja realizuje usunięcie harmonogramu po uprzedniej weryfikacji wersji obiektu oraz uprawnień prowadzącego do edycji danego przedmiotu.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
public void deleteSchedule(UUID scheduleId, String userLogin, String versionHash) {
    Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(ScheduleNotFoundException::new);

    if (!versionHash.equals(versionHashService.hashObjectVersion(scheduleId, schedule.getVersion()))) {
        throw new ConcurrentUpdateException();
    }

    Subject subject = schedule.getRulePreset().getSubject();
    boolean hasPermission = subjectAssignmentRepository.hasEditPermissions(subject, userLogin);

    if (!hasPermission) {
        throw new NoPermissionToEditScheduleException();
    }

    scheduleRepository.delete(schedule);
}
```

Listing 19.17.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.17.

Transakcja jest inicjowana przez metodę `deleteSchedule(UUID scheduleId, String userLogin, String versionHash)` z klasy `ScheduleService`, którą przedstawiono
na listingu 19.17.1. W ramach tej transakcji wywoływane są metody komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są to:
- `scheduleRepository.findById(scheduleId)`
- `subjectAssignmentRepository.hasEditPermissions(subject, userLogin)`
- `scheduleRepository.delete(schedule)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- `Schedule`
- `Subject`
- `SubjectAssignment`

### MPP.18 Przeglądanie repozytoriów

W tym przypadku użycia zastosowano jedną transakcję o charakterze wyłącznie odczytowym.

Dana transakcja realizuje odczyt wszystkich repozytoriów które są powiązane z konkretnym przedmiotem.

```java
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")  
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")  
public List<Repository> getRepositories(UUID subjectId) {  
    return giteaRepositoryRepository.findBySubjectId(subjectId);  
}
```
19.18.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.18.

Transakcja jest inicjowana przez metodę `getRepositories(UUID subjectId)` z klasy `RepositoryService`, którą przedstawiono 
na listingu 19.18.1. W ramach tej transakcji wywoływana jest metoda komponentu dostępowego, która dołącza do niej z propagacją `MANDATORY`.

W ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją MANDATORY:
* `giteaRepositoryRepository.findBySubjectId(subjectId)`

Operacja ta wykonywana jest na encji JPA **Repository**.

### MPP.19 Przeglądanie kont użytkowników

W tym przypadku użycia zastosowano jedną transakcję do odczytu.

Transakcja pobiera listę kont studentów, które są powiązane z repozytoriami przypisanymi do danego przedmiotu.

```java
 @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<SubjectStudentStatsDTO> getUserAccounts(String subjectName) {
        List<Repository> repos = giteaRepositoryRepository.findBySubject_Name(subjectName);
        List<SubjectStudentStatsDTO> results = new ArrayList<>();

        repos.forEach(repo -> {
            repo.getStudentList().forEach(student -> {
                Account account = student.getAccount();

                results.add(new SubjectStudentStatsDTO(
                        student.getId(),
                        account.getName(),
                        account.getSurname(),
                        repo.getId(),
                        repo.getRepositoryName(),
                        null
                ));
            });
        });

        return results;
    }
```

Listing 19.19.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.19.

Z uwagi na definicję parametrów transakcji na poziomie klasy, wyglądają one następująco:

```java
@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class RepositoryService {
```

Listing 19.19.2. Ustawienia transakcji aplikacyjnej w ramach przypadku MPP.19.

Transakcja jest inicjowana przez metodę getUserAccounts(String subjectName) z klasy RepositoryService, którą przedstawiono na listingu 19.19.1. W ramach transakcji wywoływana jest metoda komponentu z propagacją REQUIRES_NEW.

Jest to metoda:
- giteaRepositoryRepository.findBySubject_Name(subjectName)

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- Repository,
- Student,
- Account.

### MPP 20 Sprawdzenie swojego kodu

W tym przypadku użycia zastosowano transakcję aplikacyjną o charakterze zapisu.

Transakcja weryfikuje dostęp studenta do repozytorium, sprawdza ilość dostępnych tokenów i generuje nowy raport antyplagiatowy wraz z wyliczeniem i aktualizacją statystyk całego przedmiotu.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO generateStudentReport(UUID repositoryId, String studentTag, String taskTag, String login) {
        Repository repository = giteaRepositoryRepository.findByIdWithLock(repositoryId)
                .orElseThrow(RepositoryNotFoundException::new);

        boolean isStudentInThisRepository = repository.getStudentList().stream()
                .anyMatch(s -> s.getAccount().getLogin().equals(login));

        if (!isStudentInThisRepository) {
            throw new SubjectAccessDeniedException();
        }

        Subject subject = repository.getSubject();
        if (subject.getArchived()) {
            throw new SubjectArchivedException();
        }
        RulePreset preset = subject.getRulePreset();

        if (preset == null || "NOTHING".equals(preset.getRaportLevel().getName())) {
            throw new SubjectAccessDeniedException();
        }

        int usedTickets = reportsRepository.countReportsByIdAndCreatedByLogin(repositoryId, login);

        if (usedTickets >= preset.getStudentTicketCount()) {
            throw new NoTicketsLeftException();
        }

        List<GiteaRepoDTO> giteaRepoDTO = giteaService.getRepositoriesFromOrganization(subject.getOrganizationName());

        giteaRepoDTO.stream().filter(repo -> !repo.template()).forEach(repo -> {
            File targetDir = new File("repos/submissions/" + subject.getOrganizationName() + "/" + studentTag + "/" + repo.name());
            String tag = repo.name().equals(repository.getRepositoryName()) ? studentTag : taskTag;
            GitService.cloneTag(repo.clone_url(), tag, targetDir, giteaToken);
        });

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subject.getId());

        Report report = new Report();
        report.setSubject(subject);
        report.setTag(studentTag);
        report.setPathToReport("reports/" + subject.getOrganizationName() + "/" + studentTag + "/" + timestamp + ".jplag");
        report.setScannedRepositories(0L);
        report.setIsStudentReport(true);
        report.setRepositoryList(repositoryList);

        JPlagResult result = jPlagService.runPlagiarismScanOnOrganisation(
                subject.getOrganizationName(),
                studentTag,
                preset.getMinimumTokensMatch(),
                preset.getEnableNormalization(),
                report.getPathToReport()
        );

        List<JPlagComparison> comparisons = result.getAllComparisons();

        Double avg = comparisons.stream()
                .mapToDouble(JPlagComparison::similarity)
                .average()
                .orElse(0.0);
        report.setAverageSimilarity(avg);

        reportsRepository.saveAndFlush(report);
        subjectRepository.saveAndFlush(subject);

        return getStudentReportDetails(report.getId(), login);
    }
```

Listing 19.20.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.20.

Transakcja jest inicjowana przez metodę generateStudentReport(UUID repositoryId, String studentTag, String taskTag, String login) z klasy ReportService, którą przedstawiono na listingu 19.20.1. W ramach tej transakcji wywoływane są metody komponentów z propagacją REQUIRES_NEW. Dodatkowo, pierwsza metoda korzysta z  blokady pesymistycznej na poziomie bazy danych (findByIdWithLock).

Wywoływane metody to:
- giteaRepositoryRepository.findByIdWithLock(repositoryId)
- reportsRepository.countReportsByIdAndCreatedByLogin(repositoryId, login)
- giteaService.getRepositoriesFromOrganization(subject.getOrganizationName())
- giteaRepositoryRepository.getAllBySubjectId(subject.getId())
- reportsRepository.saveAndFlush(report)
- subjectRepository.saveAndFlush(subject)

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
- Repository,
- Student,
- Account,
- Subject,
- RulePreset,
- Report.

### MPP 21: Przeglądanie swoich raportów

W tym przypadku użycia zastosowano dwie oddzielne transakcje aplikacyjne o charakterze wyłącznie odczytowym.

Pierwsza transakcja realizuje pobranie listy wszystkich raportów antyplagiatowych, do których dany student ma nadany dostęp (zależny od konfiguracji przedmiotu).

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public List<ReportDTO> getStudentOwnReports(String login) {
        List<Report> reports = reportsRepository.findVisibleReportsForStudent(login);

        return reports.stream()
                .map(r -> new ReportDTO(
                        r.getId(),
                        r.getTag(),
                        r.getAverageSimilarity(),
                        r.getCreatedAt(),
                        r.getSubject().getCreatedBy().getLogin(),
                        r.getSubject().getName(),
                        r.getScannedRepositories()
                ))
                .toList();
    }
```
Listing 19.21.1. Metoda inicjująca pierwszą transakcję aplikacyjną w ramach przypadku MPP.21, odpowiedzialna za odczyt listy raportów.

Transakcja jest inicjowana przez metodę `getStudentOwnReports(String login)` z klasy `ReportService`, którą przedstawiono na listingu 19.21.1. W ramach tej transakcji wywoływana jest metoda komponentu dostępowego, która dołącza do niej z propagacją `MANDATORY`.

W ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją `MANDATORY`:

* `reportsRepository.findVisibleReportsForStudent(login)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:

* `Report`
* `Subject`
* `Account` (poprzez powiązanie z twórcą przedmiotu)

Druga transakcja odpowiada za odczyt szczegółów konkretnego raportu, weryfikację uprawnień studenta do jego zawartości oraz odpowiednie przefiltrowanie wyników w zależności od reguł (poziomu raportu), aby ukryć dane wrażliwe innych studentów.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO getStudentReportDetails(UUID reportId, String login) {
        Report report = reportsRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        boolean hasAccess = report.getRepositoryList().stream()
                .flatMap(repo -> repo.getStudentList().stream())
                .anyMatch(student -> student.getAccount().getLogin().equals(login));

        if (!hasAccess) {
            throw new SubjectAccessDeniedException();
        }

        if (report.getSubject() == null || report.getSubject().getRulePreset() == null || report.getSubject().getRulePreset().getRaportLevel() == null) {
            throw new SubjectAccessDeniedException();
        }

        String raportLevel = report.getSubject().getRulePreset().getRaportLevel().getName();

        List<String> studentRepoNames = report.getRepositoryList().stream()
                .filter(repo -> repo.getStudentList().stream()
                        .anyMatch(s -> s.getAccount().getLogin().equals(login)))
                .map(Repository::getRepositoryName)
                .toList();

        List<FilteredComparisonDTO> filteredMatches = new ArrayList<>();

        if (!"NOTHING".equals(raportLevel)) {
            List<TeacherComparisonDTO> allComparisons = JplagReportParser.parseFullTeacherReport(report.getPathToReport());

            filteredMatches = allComparisons.stream()
                    .filter(c -> studentRepoNames.contains(c.firstSubmission()) || studentRepoNames.contains(c.secondSubmission()))
                    .map(c -> {
                        Double studentSimilarity = null;
                        Double otherSimilarity = null;
                        Double avgSim = null;

                        boolean isFirst = studentRepoNames.contains(c.firstSubmission());

                        if ("ONLY_HIGHEST_PERCENT".equals(raportLevel)) {
                            studentSimilarity = c.maxSimilarity();
                        } else if ("ONLY_PERCENTAGES".equals(raportLevel)) {
                            studentSimilarity = c.maxSimilarity();
                            avgSim = c.averageSimilarity();
                        }
                        return new FilteredComparisonDTO("ANONYMIZED", studentSimilarity, otherSimilarity, avgSim);
                    })
                    .sorted((c1, c2) -> Double.compare(
                            c2.getStudentSimilarity() != null ? c2.getStudentSimilarity() : 0.0,
                            c1.getStudentSimilarity() != null ? c1.getStudentSimilarity() : 0.0
                    ))
                    .toList();

            if ("ONLY_HIGHEST_PERCENT".equals(raportLevel) && !filteredMatches.isEmpty()) {
                filteredMatches = List.of(filteredMatches.get(0));
            }
        }
            return new StudentReportDetailsDTO(
                    report.getId(),
                    report.getTag(),
                    report.getSubject().getName(),
                    report.getCreatedAt(),
                    filteredMatches,
                    raportLevel
            );
    }
```
Listing 19.21.2. Metoda inicjująca drugą transakcję aplikacyjną w ramach przypadku MPP.21, odpowiedzialna za szczegóły raportu studenckiego.

Transakcja jest inicjowana przez metodę `getStudentReportDetails(UUID reportId, String login)` z klasy `ReportService`, którą przedstawiono na listingu 19.21.2. Podobnie jak wcześniej, w ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją `MANDATORY`.

W ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją `MANDATORY`:

* `reportsRepository.findById(reportId)`

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA (weryfikacja dostępu i konfiguracji widoczności ukrytej w powiązaniach encji):

* `Report`
* `Repository`
* `Student`
* `Account`
* `Subject`
* `RulePreset`
* `RaportLevel`

### MPP.22 Wygeneruj raport według harmonogramu

W tym przypadku użycia zastosowano cztery transakcje aplikacyjne. Przypadek użycia jest wyzwalany cyklicznie (co minutę) 
przez mechanizm schedulera poprzez adnotację @Scheduled, a nie przez bezpośrednie żądanie użytkownika.

Główna metoda serwisu posiada propagację NOT_SUPPORTED i nie inicjuje własnej transakcji, ponieważ wykonuje 
czasochłonne operacje zewnętrzne (klonowanie repozytoriów Git z usługi Gitea, skanowanie antyplagiatowe za pomocą 
biblioteki JPlag).

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = false, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public boolean generateReportBySchedule() {
        ScheduleConfigDTO config = reportDatabaseService.fetchNextScheduleToExecute();
        if (config == null) return false;
        
        try {
            Subject subject = subjectRepository.findById(config.subjectId()).orElseThrow(SubjectNotFoundException::new);
            if (subject.getArchived()) {
                throw new SubjectArchivedException();
            }

            List<GiteaRepoDTO> giteaRepoList = giteaService.getRepositoriesFromOrganization(config.organizationName());

            long successfulClonesCount = giteaRepoList.stream()
                    .filter(repo -> !repo.template())
                    .map(repo -> {
                        File targetDir = new File("repos/submissions/" + config.organizationName() + "/" + config.giteaTag() + "/" + repo.name());
                        return GitService.cloneTag(repo.clone_url(), config.giteaTag(), targetDir, giteaToken);
                    })
                    .filter(success -> success)
                    .count();

            if (successfulClonesCount == 0) {
                throw new GitCloneException();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String reportPath = "reports/" + config.organizationName() + "/" + config.giteaTag() + "/" + timestamp + ".jplag";
            
            JPlagResult result = jPlagService.runPlagiarismScanOnOrganisation(
                    config.organizationName(),
                    config.giteaTag(),
                    config.minimumTokensMatch(),
                    config.enableNormalization(),
                    reportPath
            );

            Double avg = result.getAllComparisons().stream()
                    .mapToDouble(JPlagComparison::similarity)
                    .average()
                    .orElse(0.0);

            reportDatabaseService.saveScheduleSuccess(config, successfulClonesCount, reportPath, avg);

            subjectStatisticsService.recalculateAndUpdateStatistics(config.subjectId());
            
        } catch (Exception e) {
            reportDatabaseService.saveScheduleFailure(config.scheduleId());
        }
        return true;
    }
```
Listing 19.22.1. Metoda orkiestrująca generowanie raportu według harmonogramu, nierozpoczynająca własnej transakcji 
w ramach przypadku MPP.22.

Pierwsza transakcja w ramach MPP.22 jest inicjowana przez metodę fetchNextScheduleToExecute() z ReportDatabaseService. 
Jej zadaniem jest pobranie kolejnego harmonogramu do wykonania (pesymistycznie blokując wiersz) oraz zmapowanie 
jego danych do obiektu ScheduleConfigDTO.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public ScheduleConfigDTO fetchNextScheduleToExecute() {
        List<Schedule> schedules = scheduleRepository.findSchedulesToExecute(PageRequest.of(0, 1));
        if (schedules.isEmpty()) return null;
    
        Schedule schedule = schedules.getFirst();
        RulePreset settings = schedule.getRulePreset();
        Subject subject = settings.getSubject();
    
        return new ScheduleConfigDTO(
                schedule.getId(),
                subject.getId(),
                subject.getOrganizationName(),
                schedule.getTag(),
                settings.getMinimumTokensMatch(),
                settings.getEnableNormalization()
        );
    }
```
Listing 19.22.2. Metoda inicjująca pierwszą transakcję aplikacyjną w ramach MPP.22.

W ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją MANDATORY:
* ScheduleRepository.findSchedulesToExecute(Pageable)

Operacja ta wykonywana jest w granicach transakcji na następujących obiektach encji JPA:
* `Schedule`
* `RulePreset`
* `Subject`

Druga transakcja – w ścieżce sukcesu – inicjowana jest przez metodę saveScheduleSuccess(...) z ReportDatabaseService 
i realizuje utrwalenie wygenerowanego raportu oraz oznaczenie harmonogramu jako wykonanego.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public void saveScheduleSuccess(ScheduleConfigDTO config, long scannedCount, String reportPath, Double avgSimilarity) {
        Subject subject = subjectRepository.findById(config.subjectId()).orElseThrow(SubjectNotFoundException::new);
        List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(config.subjectId());

        Report report = new Report();
        report.setSubject(subject);
        report.setTag(config.giteaTag());
        report.setScannedRepositories(scannedCount);
        report.setIsStudentReport(false);
        report.setPathToReport(reportPath);
        report.setRepositoryList(repositoryList);
        report.setAverageSimilarity(avgSimilarity);

        reportsRepository.saveAndFlush(report);

        Schedule schedule = scheduleRepository.findById(config.scheduleId()).orElseThrow();
        schedule.setExecuted(true);
        scheduleRepository.saveAndFlush(schedule);
    }
```
Listing 19.22.3. Metoda inicjująca drugą transakcję aplikacyjną w ramach MPP.22.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją MANDATORY:
* `SubjectRepository.findById(UUID)`
* `GiteaRepositoryRepository.getAllBySubjectId(UUID)`
* `ReportsRepository.saveAndFlush(Report)`
* `ScheduleRepository.findById(UUID)`
* `ScheduleRepository.saveAndFlush(Schedule)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
* `Subject`
* `Repository`
* `Report`
* `Schedule`

Trzecia transakcja realizuje przeliczenie zagregowanej wartości średniego podobieństwa dla przedmiotu 
(identyczna metoda jak w MPP.2 – listing 19.2.4).

W przypadku wystąpienia błędu w trakcie generowania raportu inicjowana jest czwarta transakcja przez metodę 
`saveScheduleFailure(UUID scheduleId)` z `ReportDatabaseService`, oznaczająca harmonogram jako wykonany z błędem.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public void saveScheduleFailure(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        schedule.setExecuted(true);
        schedule.setHasFailed(true);
        scheduleRepository.saveAndFlush(schedule);
    }
```
Listing 19.22.4. Metoda inicjująca czwartą transakcję aplikacyjną w ramach MPP.22

W ramach tej transakcji wywoływane są metody komponentu dostępowego z propagacją `MANDATORY`:
* `ScheduleRepository.findById(UUID)`
* `ScheduleRepository.saveAndFlush(Schedule)`

Operacja ta wykonywana jest w granicach transakcji na encji JPA `Schedule`.
### MPP.24 Zmiana kierownika przedmiotu
W tym przypadku użycia zastosowano jedną transakcję o charakterze zapisu z zastosowaniem mechanizmu blokad optymistycznych.

Dana transakcja realizuje przekazanie roli właściciela `OWNER` przedmiotu innemu prowadzącemu. 
Dotychczasowy właściciel otrzymuje rolę `VIEWER`, a nowy prowadzący, o ile nie był jeszcze przypisany do przedmiotu, 
zostanie automatycznie dodany z rolą `OWNER`. Operacja zabezpieczona jest weryfikacją wersji encji przed zapisem.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public void changeSubjectManager(UUID subjectId, String newManagerLogin, String versionHash) {

        Subject subject = subjectRepository.findByIdForceIncrement(subjectId).orElseThrow(SubjectNotFoundException::new);

        checkIfNotArchived(subject);

        if (!versionHash.equals(versionHashService.hashObjectVersion(subjectId, subject.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        Teacher oldOwner = teacherRepository.findSubjectOwnerBySubjectId(subjectId)
                .orElseThrow(SubjectAssignmentNotFoundException::new);
        SubjectAssignment oldOwnerAssignment = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(oldOwner.getAccount().getLogin(), subjectId)
                .orElseThrow(SubjectAssignmentNotFoundException::new);

        Teacher newOwner = teacherRepository.findActiveTeacherByLogin(newManagerLogin)
                .orElseThrow(AccessLevelNotAssignedException::new);

        SubjectAssignment newOwnerAssignment = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(newOwner.getAccount().getLogin(), subjectId)
                .orElseGet(
                        () -> {
                            SubjectAssignment assignment = new SubjectAssignment();
                            assignment.setSubject(subject);
                            assignment.setTeacher(newOwner);
                            return assignment;
                        }
                );

        oldOwnerAssignment.setSubjectRole(TeacherSubjectRole.VIEWER);
        newOwnerAssignment.setSubjectRole(TeacherSubjectRole.OWNER);

        subjectAssignmentRepository.saveAndFlush(oldOwnerAssignment);
        subjectAssignmentRepository.saveAndFlush(newOwnerAssignment);
    }
```
Listing 19.24.1. Metoda inicjująca transakcję aplikacyjną w ramach przypadku MPP.24.

Transakcja jest inicjowana przez metodę `changeSubjectManager(UUID subjectId, String newManagerLogin, String versionHash)` 
z klasy `SubjectService`, którą przedstawiono na listingu 19.24.1. W ramach tej transakcji wywoływana jest sekwencja metod 
komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są nimi kolejno:
* `SubjectRepository.findByIdForceIncrement(UUID)`
* `TeacherRepository.findSubjectOwnerBySubjectId(UUID)`
* `SubjectAssignmentRepository.findByTeacherLoginAndSubjectId(String, UUID)`
* `TeacherRepository.findActiveTeacherByLogin(String)`
* `SubjectAssignmentRepository.saveAndFlush(SubjectAssignment)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
* `Subject`
* `Teacher`
* `SubjectAssignment`
### MPP.25 Przeglądanie szczegółów przedmiotu

Na powyższy przypadek użycia składają się dwie transakcje aplikacyjne o charakterze wyłącznie odczytowym.

Pierwsza z nich realizuje odczyt szczegółowych informacji o przedmiocie (w tym konfiguracji reguł skanowania antyplagiatowego oraz przypisanego harmonogramu) na potrzeby widoku prowadzącego, natomiast druga odpowiada za weryfikację dostępu oraz pobranie szczegółów przedmiotu dla zalogowanego studenta wraz ze statystykami wykorzystania biletów.

```java

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public SubjectDTO getSubjectDetails(UUID subjectId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        var authorities = authentication.getAuthorities();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        Optional<SubjectDescriptionTranslation> originalDescriptionE = subjectDescriptionTranslationRepository
                .findBySubjectIdAndIsOriginalTrue(subjectId);
        String originalDescription = originalDescriptionE.map(SubjectDescriptionTranslation::getTranslatedDescription).orElse("");


        UUID templateId = null;
        RulePresetDTO manualRules = null;
        RulePreset preset = subject.getRulePreset();

        if (preset != null) {
            if (preset.getIsPreset()) {
                templateId = preset.getId();
            } else {
                manualRules = ruleMapper.toRuleDTO(preset);
            }
        }

        String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());
        LocalDateTime nextScan = scheduleRepository.findNextScanTimeBySubjectId(subjectId).orElse(null);


        if (hasRole(authorities, AccountConsts.ROLE_TEACHER)) {
            TeacherSubjectRole role = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(login, subjectId)
                    .stream()
                    .findFirst()
                    .map(SubjectAssignment::getSubjectRole)
                    .orElseThrow(SubjectAccessDeniedException::new);

            return subjectMapper.toSubjectDTO(
                    subject,
                    hash,
                    role == TeacherSubjectRole.OWNER || role == TeacherSubjectRole.EDITOR,
                    role == TeacherSubjectRole.OWNER,
                    true,
                    templateId,
                    manualRules,
                    originalDescription,
                    nextScan
            );
        }

        throw new SubjectAccessDeniedException();
    }
```
Listing 19.25.1. Metoda pobierająca szczegóły przedmiotu dla prowadzącego i inicjująca pierwszą transakcję aplikacyjną dla przypadku MPP.25.

Transakcja jest inicjowana przez metodę `getSubjectDetails(UUID)` z komponentu `SubjectService`, którą przedstawiono na listingu 19.25.1. W ramach tej transakcji wywoływana jest sekwencja metod komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są nimi kolejno:
- `SubjectRepository.findById(UUID)`
- `SubjectDescriptionTranslationRepository.findBySubjectIdAndIsOriginalTrue(UUID)`
- `ScheduleRepository.findNextScanTimeBySubjectId(UUID)`
- `SubjectAssignmentRepository.findByTeacherLoginAndSubjectId(String, UUID)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
- `Subject`
- `SubjectDescriptionTranslation`
- `Schedule`
- `SubjectAssignment`
- `RulePreset`

```java
    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public StudentSubjectDetailsDTO getStudentSubjectDetails(UUID subjectId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        Optional<SubjectDescriptionTranslation> originalDescriptionE = subjectDescriptionTranslationRepository
                .findBySubjectIdAndIsOriginalTrue(subjectId);
        String originalDescription = originalDescriptionE.isPresent() ? originalDescriptionE.get().getTranslatedDescription() : "";

        if (!subjectRepository.isStudentAssignedToSubject(subjectId, login)) {
            throw new SubjectAccessDeniedException();
        }

        Repository studentRepo = subject.getRepositoryList().stream()
                .filter(repo -> repo.getStudentList().stream()
                        .anyMatch(s -> s.getAccount().getLogin().equals(login)))
                .findFirst()
                .orElse(null);

        String visibilityLevel = "NOTHING";
        if (subject.getRulePreset() != null && subject.getRulePreset().getRaportLevel() != null) {
            visibilityLevel = subject.getRulePreset().getRaportLevel().getName();
        }
        int usedTickets = studentRepo != null? reportsRepository.countReportsByIdAndCreatedByLogin(studentRepo.getId(), login) : 0;
        return new StudentSubjectDetailsDTO(
                subject.getId(),
                subject.getName(),
                subject.getOrganizationName(),
                subject.getEdition(),
                originalDescription,
                subject.getGiteaURL(),
                studentRepo != null ? repositoryMapper.toDto(studentRepo , usedTickets ) : null,
                visibilityLevel
        );
    }
```
Listing 19.25.2. Metoda pobierająca szczegóły przedmiotu dla studenta i inicjująca drugą transakcję aplikacyjną dla przypadku MPP.25.

Druga transakcja w ramach MPP.25 jest inicjowana przez metodę `getStudentSubjectDetails(UUID)` z komponentu `SubjectService`, przedstawioną na listingu 19.25.2. W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
- `SubjectRepository.findById(UUID)`
- `SubjectDescriptionTranslationRepository.findBySubjectIdAndIsOriginalTrue(UUID)`
- `SubjectRepository.isStudentAssignedToSubject(UUID, String)`
- `ReportsRepository.countReportsByIdAndCreatedByLogin(Long, String)`

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA:
- `Subject`
- `SubjectDescriptionTranslation`
- `Repository`
- `Report`

### MPP.26 Zarchiwizuj przedmiot
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna. 

Realizuje ona operację zmiany statusu archiwizacji istniejącego przedmiotu, z jednoczesnym wymuszeniem podbicia wersji encji na poziomie bazy danych, co chroni przed współbieżnymi modyfikacjami.

```java
    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void toggleArchiveSubject(UUID subjectId, String versionHash) {
        Subject subject = subjectRepository.findByIdForceIncrement(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        String currentHash = versionHashService.hashObjectVersion(subjectId, subject.getVersion());
        if (versionHash == null || !versionHash.equals(currentHash)) {
            throw new ConcurrentUpdateException();
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = subject.getSubjectAssignmentList().stream()
                .anyMatch(a -> a.getTeacher().getAccount().getLogin().equals(authentication.getName()) && a.getSubjectRole() == TeacherSubjectRole.OWNER);

        if (!isOwner) {
            throw new SubjectAccessDeniedException();
        }

        subject.setArchived(subject.getArchived() == null || !subject.getArchived());
        subjectRepository.saveAndFlush(subject);
    }
```
Listing 19.26.1. Metoda archiwizująca przedmiot i inicjująca transakcję aplikacyjną dla przypadku MPP.26.

Transakcja jest inicjowana przez metodę `toggleArchiveSubject(UUID, String)` z komponentu `SubjectService`, którą przedstawiono na listingu 19.26.1. W ramach tej transakcji wywoływana jest sekwencja metod komponentów dostępowych, które dołączają do niej z propagacją `MANDATORY`.

Są nimi kolejno:

- `SubjectRepository.findByIdForceIncrement(UUID)`
- `SubjectRepository.saveAndFlush(Subject)`

Operacje te wykonywane są w granicach transakcji na następującym obiekcie encji JPA:
- `Subject`

### Blokady optymistyczne 

Ochrona przed współbieżnymi modyfikacjami w Module Poszukiwania Plagiatu (MPP) opiera się na skrócie wersji encji 
`versionHash` przekazywanym między warstwą prezentacji a warstwą logiki biznesowej. Podejście to zastosowano w 
przypadkach: MPP.8, MPP.9, MPP.12, MPP.13, MPP.17, MPP.24 i MPP.26.

Każda metoda odczytująca dane modyfikowalne jak `getSubjectDetails(UUID)` lub `getRulePresetsTemplates()` zwraca 
w obiekcie DTO skrót wyliczany przez `VersionHashService.hashObjectVersion(UUID id, Long version)`. 
Klient przesyła go z powrotem w nagłówku `If-Match` przy każdym żądaniu zapisu. Metoda modyfikująca weryfikuje zgodność 
tego skrótu z bieżącą wersją encji. Niezgodność oznacza, że obiekt zmienił się w międzyczasie i skutkuje 
rzuceniem `ConcurrentUpdateException` oraz wycofaniem transakcji. Adnotacja `@Retryable` zapewnia automatyczne ponowienie 
próby po upływie czasu określonego w parametrze backoff.

Wiarygodność mechanizmu gwarantuje fakt, że skrót powstaje z pary identyfikatora encji i pola `@Version`. 
Wartości tej nie da się sfałszować po stronie klienta. Niezależnie od tego JPA samodzielnie weryfikuje pole `@Version` 
przy każdym wysłaniu kwerend do bazy (flush-u) i w przypadku konfliktu rzuca `OptimisticLockException`.

W przypadkach MPP.8, MPP.24 i MPP.26 zamiast zwykłego `findById` stosowana jest metoda `findByIdForceIncrement` 
z adnotacją `@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)`:

```java
@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
@Query("SELECT s FROM Subject s WHERE s.id = :id")
Optional<Subject> findByIdForceIncrement(@Param("id") UUID id);
```
Listing 19.27.1. Metoda repozytorium z typem blokady OPTIMISTIC_FORCE_INCREMENT w SubjectRepository.

Jej użycie gwarantuje bezwarunkowe podbicie `@Version` encji `Subject` przy zatwierdzeniu transakcji, niezależnie od tego, 
czy którekolwiek pole zostało faktycznie zmienione. Każda inna transakcja próbująca zapisać ten sam obiekt z nieaktualną 
wersją zostanie przez JPA odrzucona. 

Przykład zastosowania widoczny jest na listingach 19.26.1 dla MPP.26 oraz 19.8.1 dla MPP.8.

### Blokady pesymistyczne 

W module MPP zastosowano dwie blokady pesymistyczne, każda adresująca inny rodzaj wyścigu.

Pierwsza dotyczy pobierania harmonogramów do wykonania i znajduje się w metodzie `findSchedulesToExecute(Pageable)` 
repozytorium `ScheduleRepository`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE) 
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) 
    @Query(value = "select s from Schedule s where s.scheduleDateTime <= CURRENT_TIMESTAMP and s.isExecuted = false")
    List<Schedule> findSchedulesToExecute(Pageable pageable);
```
Listing 19.28.1. Blokada pesymistyczna w ScheduleRepository.findSchedulesToExecute.

Wartość timeout `-2` odpowiada dyrektywie `SKIP LOCKED`. Wiersz już zablokowany przez inną instancję aplikacji jest pomijany, 
zamiast blokować bieżące żądanie. Przy wdrożeniu wieloinstancyjnym każdy węzeł pobiera tym samym inny harmonogram, 
eliminując ryzyko dwukrotnego wygenerowania tego samego raportu. Ostatecznie aplikacja działa w konfiguracji 
jednoinstancyjnej, jednak blokada pozostaje jako zabezpieczenie na wypadek zmiany tej decyzji. Blokada jest zakładana 
w ramach transakcji `fetchNextScheduleToExecute()` (MPP.22) i zwalniana po jej zakończeniu.

Druga blokada zabezpiecza operację sprawdzenia kodu przez studenta i zdefiniowana jest w metodzie `findByIdWithLock(UUID)`
repozytorium `GiteaRepositoryRepository`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Repository r WHERE r.id = :id")
    Optional<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> findByIdWithLock(@Param("id") UUID id);
```
Listing 19.28.2. Blokada pesymistyczna w GiteaRepositoryRepository.findByIdWithLock.

Metoda wywoływana jest w metodzie `generateStudentReport(...)` klasy `ReportService` (MPP.20). Zablokowanie wiersza 
`Repository`na czas całej transakcji uniemożliwia równoległe wejście innego wątku, co mogłoby nastąpić, gdyby student 
wysłał kilka żądań skanowania jednocześnie. Bez tej blokady weryfikacja limitu biletów `countReportsByIdAndCreatedByLogin` 
byłaby podatna na wyścig i student mógłby przekroczyć dozwoloną liczbę skanowań.