# Student Management Service

A Spring Boot (Java 21) service managing Students, Courses, Enrollments, Grades, Attendance, Addresses, and Contacts with a minimal HTML/Vanilla JS frontend (served from `/`).

---
## 1. Table of Contents
1. Overview & Architecture  
2. Technology Stack  
3. Identifier & Data Model Strategy  
4. Entity Schemas & DTO Shapes  
5. Enumerations & Value Mappings  
6. REST API Endpoints (Full Reference)  
7. Aggregate Student Creation Contract  
8. CSV Upload Formats  
9. Validation Rules  
10. Error Model & Status Codes  
11. Delete Cascade Behavior  
12. Frontend Interaction Notes  
13. Build / Run / Test  
14. Environment & Configuration  
15. Troubleshooting  
16. Roadmap / Enhancements

---
## 2. Overview & Architecture
A layered Spring Boot service:
- Controller layer: REST endpoints (JSON in/out) & CSV upload handlers.
- Service layer: Business rules (validation beyond bean constraints, cascade orchestration).
- Repository layer: Spring Data JPA repositories.
- Persistence: PostgreSQL (primary), H2 (tests) using a unified `schema.sql`.
- Frontend: Single `index.html` + `app.js` performing fetches and inline CRUD.

---
## 3. Technology Stack
| Category | Choice |
|----------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 (Web, Validation, JPA) |
| DB | PostgreSQL (prod) / H2 (tests, MODE=PostgreSQL) |
| Build | Gradle |
| ORM | Hibernate 6.x |
| JSON | Jackson (Spring Boot default) |
| Logging | SLF4J + Logback |
| Validation | Jakarta Bean Validation |
| Frontend | Static HTML/CSS + Vanilla JS |

---
## 4. Identifier & Data Model Strategy
All entities use a single numeric primary key named `<entity>Number` (`BIGSERIAL` in Postgres). UUIDs were removed to simplify debugging and indexing. Foreign keys reference these numeric keys.

| Entity | PK Column |
|--------|-----------|
| StudentData | student_number |
| CourseDetails | course_number |
| EnrollmentData | enrollment_number |
| Grades | grade_number |
| AttendanceData | attendance_number |
| AddressDetails | address_number |
| ContactDetails | contact_number |

---
## 5. Entity Schemas & DTO Shapes
DTOs (request/response) mirror entity fields but use primitive / String representations for convenience. Below: fields **required unless noted**.

### 5.1 StudentDataDto
```jsonc
{
  "studentNumber": "123",          // string in responses, omit on create
  "firstName": "Aarav",            // max 100
  "lastName": "Sharma",            // max 100
  "dateOfBirth": "2002-04-15",     // ISO date
  "gender": 0,                      // enum ordinal (see section 6)
  "joiningDate": "2021-08-10",     // ISO date
  "studentStatus": 0                // enum ordinal
}
```

### 5.2 CourseDetailsDto
```jsonc
{
  "courseNumber": "5",             // response only
  "courseName": "Data Structures", // max 100
  "courseCode": "CS201",           // max 10
  "courseCredits": 4.0              // double
}
```

### 5.3 EnrollmentDataDto
```jsonc
{
  "enrollmentNumber": "10",         // response only
  "studentNumber": "1",             // FK (string numeric)
  "courseNumber": "2",              // FK
  "enrollmentDate": "2025-02-01",
  "overallGrade": 88,                // 0-100 (domain rule)
  "semester": "SPRING25",           // max 20
  "instructorName": "Dr Tester"     // max 100
}
```

### 5.4 GradesDto
```jsonc
{
  "gradeNumber": 7,                  // response only
  "enrollmentNumber": 10,            // FK
  "assessmentDate": "2025-03-01",
  "assessmentType": "Midterm",      // max 50
  "obtainedScore": 45,               // 0-100
  "maxScore": 50,                    // 1-100
  "gradeCode": 1                     // optional, 0-10
}
```

### 5.5 AttendanceDataDto
```jsonc
{
  "attendanceNumber": 3,             // response only
  "studentNumber": "1",             // FK
  "enrollmentNumber": 10,            // FK
  "attendanceDate": "2025-03-05",
  "attendanceStatus": 0,             // enum ordinal
  "semester": "SPRING25"            // optional, max 20
}
```

### 5.6 AddressDetailsDto
```jsonc
{
  "addressNumber": 4,                // response only
  "studentNumber": "1",             // FK
  "street": "12 MG Road",           // max 200
  "city": "Mumbai",                 // max 100
  "state": "Maharashtra",           // max 50
  "zipCode": "400001"               // max 20
}
```

### 5.7 ContactDetailsDto
```jsonc
{
  "contactNumber": 9,                // response only
  "studentNumber": "1",             // FK
  "emailAddress": "user@example.com", // max 100, @Email
  "mobileNumber": "9876543210"      // max 15
}
```

---
## 6. Enumerations & Value Mappings
| Enum | Ordinal -> Meaning |
|------|--------------------|
| Gender | 0=Male, 1=Female, 2=Other |
| StudentStatus | 0=Active, 1=Inactive, 2=Graduated |
| AttendanceStatus | 0=Present, 1=Absent, 2=Excused |

Grade code is an integer 0–10 (mapping to a grading scale is client-defined).

---
## 7. REST API Endpoints
Base path: `/api`  
Unless otherwise noted all POST respond `201 Created` with body, GET/PUT/PATCH respond `200 OK`, DELETE responds `204 No Content`.

### 7.1 Students
| Method | Path | Description | Body | Notes |
|--------|------|-------------|------|-------|
| GET | /students | List all students | – | Returns array of StudentDataDto |
| GET | /students/{studentNumber} | Get one | – | 404 if not found |
| POST | /students | Create student | StudentDataDto (no studentNumber) | Returns created DTO |
| PATCH | /students/{studentNumber} | Partial update | Partial JSON (subset of fields) | Only provided fields updated |
| PUT | /students/{studentNumber} | Full replace | Full StudentDataDto | studentNumber path authoritative |
| DELETE | /students/{studentNumber} | Delete + cascade | – | Removes related enrollments/grades/attendance, addresses, contacts |
| POST | /students/upload-csv | Bulk ingest | multipart `file` | 207 Multi-Status if partial errors |
| POST | /students/aggregate | Create student + nested | See section 8 | Skips invalid nested items |

### 7.2 Courses
| Method | Path | Body | Notes |
|--------|------|------|-------|
| GET | /courses | – | List CourseDetails (entity fields) |
| GET | /courses/{courseNumber} | – | 404 if not found |
| POST | /courses | CourseDetailsDto | Created course |
| PUT | /courses/{courseNumber} | CourseDetailsDto | Full update |
| DELETE | /courses/{courseNumber} | – | 204 if deleted |
| POST | /courses/upload-csv | multipart file | Bulk ingest |

### 7.3 Enrollments
| Method | Path | Body | Notes |
|--------|------|------|-------|
| GET | /enrollments | – | Returns entity with nested student/course objects |
| GET | /enrollments/{enrollmentNumber} | – | 404 if not found |
| POST | /enrollments | EnrollmentDataDto | Validates FK existence |
| PUT | /enrollments/{enrollmentNumber} | EnrollmentDataDto | Replace |
| DELETE | /enrollments/{enrollmentNumber} | – | 204 |
| POST | /enrollments/upload-csv | multipart file | Bulk ingest |

### 7.4 Grades
| Method | Path | Body | Notes |
|--------|------|------|-------|
| GET | /grades | – | Returns grades with nested enrollment->student+course |
| GET | /grades/{gradeNumber} | – | 404 if not found |
| POST | /grades | GradesDto | Validates enrollmentNumber |
| PUT | /grades/{gradeNumber} | GradesDto | Replace |
| DELETE | /grades/{gradeNumber} | – | 204 |
| POST | /grades/upload-csv | multipart file | Bulk ingest |

### 7.5 Attendance
| Method | Path | Body | Notes |
|--------|------|------|-------|
| GET | /attendance | – | Returns attendance with nested enrollment & student |
| GET | /attendance/{attendanceNumber} | – | 404 if not found |
| POST | /attendance | AttendanceDataDto | Requires valid student & enrollment |
| PUT | /attendance/{attendanceNumber} | AttendanceDataDto | Replace |
| DELETE | /attendance/{attendanceNumber} | – | 204 |
| POST | /attendance/upload-csv | multipart file | Bulk ingest |

### 7.6 Addresses
| Method | Path | Body | Notes |
|--------|------|------|-------|
| GET | /addresses | – | List with nested student |
| GET | /addresses/{addressNumber} | – | 404 if not found |
| POST | /addresses | AddressDetailsDto | FK student required |
| PUT | /addresses/{addressNumber} | AddressDetailsDto | Replace |
| DELETE | /addresses/{addressNumber} | – | 204 |
| POST | /addresses/upload-csv | multipart file | Bulk ingest |

### 7.7 Contacts
| Method | Path | Body | Notes |
|--------|------|------|-------|
| GET | /contacts | – | List with nested student |
| GET | /contacts/{contactNumber} | – | 404 if not found |
| POST | /contacts | ContactDetailsDto | FK student required, uniqueness enforced for email/mobile |
| PUT | /contacts/{contactNumber} | ContactDetailsDto | Replace |
| DELETE | /contacts/{contactNumber} | – | 204 |
| POST | /contacts/upload-csv | multipart file | Bulk ingest |

---
## 8. Aggregate Student Creation Contract
Endpoint: `POST /api/students/aggregate`
```jsonc
{
  "firstName": "Aarav",
  "lastName": "Sharma",
  "dateOfBirth": "2002-04-15",
  "gender": 0,
  "joiningDate": "2021-08-10",
  "studentStatus": 0,
  "addresses": [ {"street":"12 MG Road","city":"Mumbai","state":"Maharashtra","zipCode":"400001"} ],
  "contacts": [ {"emailAddress":"aarav.sharma@example.com","mobileNumber":"9876543210"} ],
  "enrollments": [ {"courseNumber": 1, "enrollmentDate": "2021-08-12", "overallGrade": 88, "semester": "Fall 2021", "instructorName": "Dr. R. Krishnan"} ]
}
```
Behavior:
- Creates student first; then each nested list item.
- Invalid nested items (e.g., missing courseNumber) are skipped silently (could be enhanced to report).

---
## 9. CSV Upload Formats
All CSV handlers require a header row. Columns are matched case-insensitively. Empty lines skipped.

| Entity | Required Columns |
|--------|------------------|
| Students | firstName,lastName,dateOfBirth,gender,joiningDate,studentStatus |
| Courses | courseName,courseCode,courseCredits |
| Enrollments | studentNumber,courseNumber,enrollmentDate,overallGrade,semester,instructorName |
| Grades | enrollmentNumber,assessmentDate,assessmentType,obtainedScore,maxScore,gradeCode |
| Attendance | studentNumber,enrollmentNumber,attendanceDate,attendanceStatus,semester |
| Addresses | studentNumber,street,city,state,zipCode |
| Contacts | studentNumber,emailAddress,mobileNumber |

Partial success returns `207 Multi-Status` with payload:
```jsonc
{
  "inserted": 5,
  "errors": ["Row 7: Invalid FK course"],
  "<entityPlural>": [ ...createdRows ]
}
```

---
## 10. Validation Rules
| Field Type | Rule |
|------------|------|
| firstName / lastName | not null, <=100 |
| dateOfBirth / joiningDate | ISO date, not null |
| gender | 0–2 valid ordinals |
| studentStatus | 0–2 ordinals |
| overallGrade / scores | 0–100 (obtainedScore), 1–100 (maxScore) |
| semester | <=20 chars, not null (except attendance optional) |
| instructorName | <=100 chars |
| emailAddress | valid format, <=100, unique |
| mobileNumber | <=15, unique |
| street / city / state / zip | length limits per DTO |

Business-level validation (existence of referenced student/course/enrollment) is enforced in services/controllers.

---
## 11. Error Model & Status Codes
Unified structure (`ErrorDto`):
```jsonc
{"status":400,"message":"Invalid enrollment data","details":"Invalid FK student or course"}
```
| Status | Scenario |
|--------|----------|
| 200 | Successful GET/PUT/PATCH |
| 201 | Resource created (POST) |
| 204 | Successful delete |
| 207 | Partial CSV ingestion |
| 400 | Validation / bad FK / malformed input |
| 404 | Resource not found |
| 500 | Unhandled server error |

---
## 12. Delete Cascade Behavior
`DELETE /students/{studentNumber}` orchestrates manual cleanup:
1. Load student; if absent -> 404.
2. Delete enrollments (grades & attendance cascade via JPA relationships).
3. Delete contacts & addresses.
4. Delete student.

(Enhancement: convert to `ON DELETE CASCADE` + JPA orphan removal for simplification.)

---
## 13. Frontend Interaction Notes
- Initial load fetches students; background prefetch grabs other datasets.
- Inline edit forms patch/put individual rows (numeric IDs only).
- Status bar communicates outcomes (info/error).
- Add forms hide after success and trigger list refresh.

---
## 14. Build / Run / Test
```bash
# Run tests
./gradlew test

# Build runnable jar
./gradlew bootJar

# Run
java -jar build/libs/StudentManagenet2025-1.0-SNAPSHOT.jar

# Smoke test endpoints
for ep in students courses enrollments grades attendance addresses contacts; do \
  curl -s -o /dev/null -w "$ep: HTTP %{http_code}\n" http://localhost:8080/api/$ep; \
 done
```

---
## 15. Environment & Configuration
`application.properties` (prod/dev): manually managed schema (ddl-auto=none).  
`application-test.properties`: H2 in PostgreSQL mode loads `schema.sql` automatically.

Key settings:
| Property | Purpose |
|----------|---------|
| spring.datasource.url | Postgres/H2 connection |
| spring.jpa.hibernate.ddl-auto | none (use schema.sql) |
| spring.sql.init.schema-locations | test-only schema initialization |
| logging.file.name | File logging under `logs/app.log` |

---
## 16. Troubleshooting
| Symptom | Likely Cause | Fix |
|---------|--------------|-----|
| 404 on fetch | Wrong numeric key | List endpoint to verify existing numbers |
| 400 invalid FK | Student/course/enrollment missing | Create prerequisite entity first |
| CSV 207 response | Some bad rows | Review `errors` array, correct CSV row(s) |
| Delete FK violation | Manual DB delete order | Use service DELETE endpoints |
| Blank frontend tables | JS error / stale cache | Hard refresh, open DevTools console |

---
## 17. Roadmap / Enhancements
- OpenAPI (springdoc) generation
- Pagination & sort query params (
  e.g., `?page=0&size=20&sort=lastName,asc`)
- Unified exception handling (ControllerAdvice)
- ON DELETE CASCADE for DB-level simplification
- Integration tests for aggregate creation & cascade delete
- Optional authentication/authorization layer (JWT)
- Caching for reference data

---
*Last updated:* (auto-maintain manually) **2025-11-06**
