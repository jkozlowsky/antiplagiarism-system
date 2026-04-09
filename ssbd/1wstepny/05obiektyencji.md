
## 5 Identyfikacja obiektów encji 2 pkt

**Zestawienie klas encji**
### 1. Klasa bazowa (Mapped Superclass)

- **Encja**: `AbstractEntity`
	- **Tabela:** nie posiada, bo jest @MappedSuperclass. Każda inna tabela dziedziczy po tej.
    - **Klucz główny**: 
	    - `id`: UUID 
		    - `@Id`  
			- `@GeneratedValue(strategy = GenerationType.UUID) ` 
			- `@Column(name = "id", updatable = false, nullable = false)`
    - Pola i ograniczenia:
	    - `version`: Integer
		    - `@Version`  
			- `@Column(name = "version", nullable = false)`
		- `createdAt`: LocalDateTime
			- `@CreatedDate`
			- `@Column(name = "created_at", nullable = false, updatable = false)`
		- `updatedAt`: LocalDateTime
			- `@UpdateTimestamp`
			- `@Column(name = "updated_at")`
	- **Zależności:**
		- `createdBy`: Account
			- `@CreatedBy`
			- `@ManyToOne(fetch = FetchType.LAZY)`
			- `@JoinColumn(name = "created_by", updatable = false)`
		- `modifiedBy`: Account
			- `@LastModifiedBy`
			- `@ManyToOne(fetch = FetchType.LAZY)`
			- `@JoinColumn(name = "modified_by")`
### 2. Moduł MOK (Moduł Obsługi Kont)

- **Encja: Account**
	- **Tabela**: account 
    - **Klucz główny:** Dziedziczony z `AbstractEntity` (`id`: UUID)
    - **Pola i ograniczenia:**
        - `login`: String
            - `@Column(name = "login", nullable = false, unique = true, updatable = false, length = AccountConsts.LOGIN_LENGTH)`
        - `passwordHash`: String
            - `@Column(name = "password_hash", nullable = false, length = AccountConsts.PASSWORD_HASH_LENGTH)`
        - `email`: String
            - `@Column(name = "email", nullable = false, unique = true, length = AccountConsts.EMAIL_LENGTH)`
        - `lastSuccessfulLoginIp`: String
            - `@Column(name = "last_successful_login_ip", length = AccountConsts.IP_LENGTH)`
        - `lastFailedLoginIp`: String
            - `@Column(name = "last_failed_login_ip", length = AccountConsts.IP_LENGTH)`
        - `lastLoginFailureDateTime`: LocalDateTime
	        - `@Column(name = "last_login_failure_date_time")`
        - `lastLoginSuccessDateTime`: LocalDateTime
	        - `@Column(name = "last_login_success_date_time")`
        - `isBlocked`: Boolean
	        - `@Column(name = "is_blocked", nullable = false)`
        - `name`: String
            - `@Column(name = "name", nullable = false, length = AccountConsts.NAME_LENGTH)`
        - `surname`: String
            - `@Column(name = "surname", nullable = false, length = AccountConsts.SURNAME_LENGTH)`
    - **Zależności:**
        - `accessLevelList`: List\<AccessLevel> 
			-  `@OneToMany(mappedBy = "account", cascade = {CascadeType.PERSIST, CascadeType.MERGE})`
        - `languageVersion`: LanguageEnum
            - `@ManyToOne(fetch = FetchType.LAZY)`
            - `@JoinColumn(name = "language_version_id", nullable = false)`
- **Encja: AccessLevel**
    - **Tabela**: access_level
    - **Klucz główny:** Dziedziczony z `AbstractEntity` (`id`: UUID)
    - **Pola i ograniczenia:**
    - **Zależności:**
	    - `accessLevelName`: AccessLevelDict
            - `@JoinColumn(name = "access_level_id", nullable = false)`
            - `@ManyToOne(fetch = FetchType.LAZY)`
        - `account`: Account
	        - `@ManyToOne(fetch = FetchType.LAZY)`
	        - `@JoinColumn(name="account_id")`
        - Dziedziczenie:
	        - `@Inheritance(strategy = InheritanceType.JOINED)`
- **Encja: Admin**
	- **Tabela**: admin
    - **Klucz główny:** Dziedziczony z `AccessLevel` (`id`: UUID)
    - **Zależności:** Rozszerza klasę `AccessLevel`
- **Encja: Student**
	- **Tabela**: student
    - **Klucz główny:** Dziedziczony z `AccessLevel` (`id`: UUID)
    - **Zależności:**
        - `repositories`: List\<Repository> 
	        - `@ManyToMany(mappedBy = "studentList")`
- **Encja: Teacher**
	- **Tabela**: teacher
    - **Klucz główny:** Dziedziczony z `AccessLevel` (`id`: UUID)       
    - **Zależności:**
        - `subjectAssignmentList`: List\<SubjectAssignment>
	        - `@OneToMany(mappedBy = "teacher")`
- **Encja: AccessLevelDict**
	- **Tabela**: access_level_dict
    - **Klucz główny:** 
	    - `id`: UUID
		    - `@Id`
		    - `@GeneratedValue(strategy = GenerationType.UUID)`
		    - `@Column(name = "id", updatable = false, nullable = false)`
    - **Pola i ograniczenia:**
	    - `accessLevelName`: String
		    - `@Column(name = "access_level_name", nullable = false, unique = true)`
	    - `version`: Integer
		    - `@Column(name = "version", nullable = false)`
		    - `@Version`
- **Encja: LanguageDict**
	- **Tabela**: language_dict
    - **Klucz główny:** 
	    - `id`: UUID
		    - `@Id`
		    - `@GeneratedValue(strategy = GenerationType.UUID)`
		    - `@Column(name = "id", updatable = false, nullable = false)`
    - **Pola i ograniczenia:**
	    - `language`: String
		    - `@Column(name = "language", nullable = false, unique = true)`
	    - `version`: Integer
		    - `@Column(name = "version", nullable = false)`
		    - `@Version`
### 3. Moduł MPP (Moduł Poszukiwania Plagiatu)

- **Encja: Subject**
	- **Tabela**: subject
    - **Klucz główny:** Dziedziczony z `AbstractEntity` (`id`: UUID)      
	- **Pola i ograniczenia:**
        - `name`: String
            - `@Column(name = "name", nullable = false, length = SubjectConsts.SUBJECT_NAME_LENGTH)`
        - `edition`: String
            - `@Column(name = "edition", nullable = false, length = SubjectConsts.SUBJECT_EDITION_LENGTH)`
        - `description`: String
            - `@Column(name = "subject_description", length = SubjectConsts.SUBJECT_DESCRIPTION_LENGTH)`
        - `giteaURL`: String
            - `@Column(name = "gitea_url", nullable = false, length = SubjectConsts.GITEA_URL_LENGTH))`
        - `archived`: Boolean
            - `@Column(name = "archived", nullable = false)`
    - **Zależności:**
        - `reportList`: List\<Report>
			- `@OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})`
        - `rulePreset: RulePreset
			-  `@OneToOne(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})`
        - `subjectAssignmentList`: List\<SubjectAssignment> 
	        - `@OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})`
	    - `repositoryList`: List\<Repository>
		    - `@OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})`
- **Encja: Repository**
	- **Tabela**: repository
    - **Klucz główny:** Dziedziczony z `AbstractEntity` (`id`: UUID)  
    - **Pola i ograniczenia:**
        - `repositoryName`: String
            - `@Column(name = "repository_name", nullable = false, length = RepositoryContsts.REPOSITORY_NAME_LENGTH)`
    - **Zależności:**
        - `subject`:  Subject
	        - `@ManyToOne(fetch = FetchType.LAZY)`
	        - `@JoinColumn(name = "subject_id", updatable = false)`
        - `studentList`: List\<Student>
	        - `@ManyToMany(fetch = FetchType.LAZY)` 
	        - ` @JoinTable(` 
		        `name = "repository_student", ` 
		        `joinColumns = @JoinColumn(name = "repository_id"),  `
		        `inverseJoinColumns = @JoinColumn(name = "student_id"),`
			        `indexes = { ` 
		        `@Index(name = "idx_repository_student_repository_id", columnList = "repository_id"),`  
		         ` @Index(name = "idx_repository_student_student_id", columnList  = "student_id") ` 
				}` 
				`)`
        - `reportList`: List\<Report>
	        - `@ManyToMany(mappedBy = "repositoryList")`
- **Encja: Report**
    - **Tabela**: report
    - **Klucz główny:** Dziedziczony z `AbstractEntity` (`id`: UUID)  
    - **Pola i ograniczenia:**
        - `pathToReport`: String
            - `@Column(name = "path_to_report", nullable = false, length = ReportConsts.PATH_LENGTH)`
    - **Zależności:**
        - `repositoryList`: List\<Repository>
	        - `@ManyToMany(fetch = FetchType.LAZY)`
	        - ` @JoinTable(` 
		        `name = "report_repository", ` 
		        `joinColumns = @JoinColumn(name = "report_id"),  `
		        `inverseJoinColumns = @JoinColumn(name = "repository_id") ` 
				`)`
        - `subject`: Subject
	        - `@ManyToOne(fetch = FetchType.LAZY)`
	        - `@JoinColumn(name = "subject_id", updatable = false)`
- **Encja: RulePreset**
	- Tabela: rule_preset
    - **Klucz główny:** Dziedziczony z `AbstractEntity` (`id`: UUID)  
    - **Pola i ograniczenia:**
        - `studentTicketCount`: Integer
            - `@Column(name = "student_ticket_count", nullable = false)`
        - `isPreset`: Boolean
            - `@Column(name = "is_preset", nullable = false)`
    - **Zależności:**
        - `raportLevel`: RaportLevelDict
            - `@ManyToOne(fetch = FetchType.LAZY)`
            - `@JoinColumn(name = "raport_level_id", nullable = false)`
        - `scheduleList`: List\<Schedule>
	        - `@OneToMany(mappedBy = "rulePreset", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})`
        - `subject`: Subject
	        - `@OneToOne(fetch = FetchType.LAZY)`,
	        - `@JoinColumn(name = "subject_id", updatable = false)`
- **Encja: Schedule**
	- Tabela: schedule
    - **Klucz główny:** Dziedziczony z `AbstractEntity` (`id`: UUID)  
    - **Pola i ograniczenia:**
        - `scheduleDateTime`: LocalDateTime
            - `@Column(name = "schedule_date_time", nullable = false)`
        - `tag`: String
            - `@Column(name = "tag", nullable = false, length = TagConsts.TAG_LENGTH)`
    - **Zależności:**
        - `rulePreset`: RulePreset
	        - `@ManyToOne(fetch = FetchType.LAZY)`
	        - `@JoinColumn(name = "rule_preset_id")`
- **Encja: SubjectAssignment**
	- Tabela: subject_assignment
    - **Klucz główny:** Dziedziczony z `AbstractEntity` (`id`: UUID)  
    - **Pola i ograniczenia:**
        - `hasEditPermissions`: Boolean
            - `@Column(name = "has_edit_permissions", nullable = false)`
    - **Zależności:**
        - `teacher`: Teacher 
	        - `@ManyToOne`
	        - `@JoinColumn(name = "teacher_id", updatable = false)`
        - `subject`: Subject
	        - `@ManyToOne`
	        - `@JoinColumn(name = "subject_id", updatable = false)`
- **Encja: RaportLevelDict
	- **Tabela**: raport_level_dict
    - **Klucz główny:** 
	    - `id`: UUID
		    - `@Id`
		    - `@GeneratedValue(strategy = GenerationType.UUID)`
		    - `@Column(name = "id", updatable = false, nullable = false)`
    - **Pola i ograniczenia:**
	    - `name`: String
		    - `@Column(name = "name", nullable = false, unique = true)`
	    - `version`: Integer
		    - `@Column(name = "version", nullable = false)`
		    - `@Version`


**Związki między obiektami klas encji**

Tabela 1.5.1. Macierz krzyżowa związków między obiektami klas encyjnych.

| **Encja A (Wiersz) \ Encja B (Kolumna)** | **Account**          | **AccessLevel**         | **Subject**          | **Repository**             | **Report**                 | **RulePreset**             | **Schedule**               | **Teacher**          | **Student**          | **SubjectAssignment** | AccessLevelDict | LanguageDict    | RaportLevelDict |
| ---------------------------------------- | -------------------- | ----------------------- | -------------------- | -------------------------- | -------------------------- | -------------------------- | -------------------------- | -------------------- | -------------------- | --------------------- | --------------- | --------------- | --------------- |
| **Account**                              | -                    | BI, MA, 1:N, C(P, M), M | -                    | -                          | -                          | -                          | -                          | -                    | -                    | -                     | -               | U,OW,N:1,Brak,I | -               |
| **AccessLevel**                          | BI, OW, N:1, Brak, M | -                       | -                    | -                          | -                          | -                          | -                          | -                    | -                    | -                     | U,OW,N:1,Brak,I | -               | -               |
| **Subject**                              | -                    | -                       | -                    | BI, MA, 1:N, C(P, M, R), I | BI, MA, 1:N, C(P, M, R), I | BI, MA, 1:1, C(P, M, R), I | -                          | -                    | -                    | BI, MA, 1:N, Brak, I  | -               | -               | -               |
| **Repository**                           | -                    | -                       | BI, OW, N:1, Brak, I | -                          | BI, MA, N:M, Brak, I       | -                          | -                          | -                    | BI, OW, N:M, Brak, M | -                     | -               | -               | -               |
| **Report**                               | -                    | -                       | BI, OW, N:1, Brak, I | BI, OW, N:M, Brak, I       | -                          | -                          | -                          | -                    | -                    | -                     | -               | -               | -               |
| **RulePreset**                           | -                    | -                       | BI, OW, 1:1, Brak, I | -                          | -                          | -                          | BI, MA, 1:N, C(P, M, R), M | -                    | -                    | -                     | -               | -               | U,OW,N:1,Brak,I |
| **Schedule**                             | -                    | -                       | -                    | -                          | -                          | BI, OW, N:1, Brak, M       | -                          | -                    | -                    | -                     | -               | -               | -               |
| **Teacher**                              | -                    | -                       | -                    | -                          | -                          | -                          | -                          | -                    | -                    | BI, MA, 1:N, Brak, I  | -               | -               | -               |
| **Student**                              | -                    | -                       | -                    | BI, MA, N:M, Brak, M       | -                          | -                          | -                          | -                    | -                    | -                     | -               | -               | -               |
| **SubjectAssignment**                    | -                    | -                       | BI, OW, N:1, Brak, I | -                          | -                          | -                          | -                          | BI, OW, N:1, Brak, I | -                    | -                     | -               | -               | -               |
| AccessLevelDict                          | -                    | -                       | -                    | -                          | -                          | -                          | -                          | -                    | -                    | -                     | -               | -               | -               |
| LanguageDict                             | -                    | -                       | -                    | -                          | -                          | -                          | -                          | -                    | -                    | -                     | -               | -               | -               |
| ReportLevelDict                          | -                    | -                       | -                    | -                          | -                          | -                          | -                          | -                    | -                    | -                     | -               | -               | -               |


Tabela 1.5.2. Kontynuacja macierzy krzyżowej związków między obiektami klas encyjnych.

| **Encja A (Wiersz) \ Encja B (Kolumna)** | **Student**          | **SubjectAssignment** | AccessLevelDict | LanguageDict    | RaportLevelDict |
| ---------------------------------------- | -------------------- | --------------------- | --------------- | --------------- | --------------- |
| **Account**                              | -                    | -                     | -               | U,OW,N:1,Brak,I | -               |
| **AccessLevel**                          | -                    | -                     | U,OW,N:1,Brak,I | -               | -               |
| **Subject**                              | -                    | BI, MA, 1:N, Brak, I  | -               | -               | -               |
| **Repository**                           | BI, OW, N:M, Brak, M | -                     | -               | -               | -               |
| **Report**                               | -                    | -                     | -               | -               | -               |
| **RulePreset**                           | -                    | -                     | -               | -               | U,OW,N:1,Brak,I |
| **Schedule**                             | -                    | -                     | -               | -               | -               |
| **Teacher**                              | -                    | BI, MA, 1:N, Brak, I  | -               | -               | -               |
| **Student**                              | -                    | -                     | -               | -               | -               |
| **SubjectAssignment**                    | -                    | -                     | -               | -               | -               |
| AccessLevelDict                          | -                    | -                     | -               | -               | -               |
| LanguageDict                             | -                    | -                     | -               | -               | -               |
| ReportLevelDict                          | -                    | -                     | -               | -               | -               |


### Legenda oznaczeń w tabeli:
- Kierunkowość
    - **U** – Związek jednokierunkowy.
    - **BI** – Związek dwukierunkowy.
    - **D** - Dziedziczenie 
- Właściciel**:
    - **OW** (Owner) – Właściciel związku.
    - **MA** (Mapped) – Strona pasywna.
- **Liczność**:
    - **1:1** – One-to-One.
    - **1:N** – One-to-Many.
    - **N:1** – Many-to-One.
    - **N:M** – Many-to-Many.
- **Kaskady**:
    - **C** (Cascade) – Określone operacje (P: PERSIST, M: MERGE, R:REFRESH).
    - **Brak** – Domyślne zachowanie JPA (brak kaskad).
- **Zmienność (Immutability)**:
    - **M** (Mutable) – Związek może ulec zmianie.
    - **I** (Immutable) – Związek nie zmienia się po ustaleniu (np. przez `updatable=false`).