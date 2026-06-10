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

TODO

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

### MPP.9 Usuwanie przedmiotu

TODO

### MPP 13: Usuwanie reguł

TODO

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

### MPP 21: Przeglądanie listy swoich raportów

TODO
