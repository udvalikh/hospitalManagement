# Hospital Management API

Spring Boot 3 / Java 17 REST API with JWT authentication for a hospital appointment system.
Roles: **ADMIN**, **DOCTOR**, **PATIENT**.

## Quick start

```bash
# 1. Create the database (drops and recreates hospital_db, loads demo data)
cd database && ./create.sh

# 2. Run the API on http://localhost:9090
./mvnw spring-boot:run
```

Demo accounts (password for all: `password`): `admin`, `drsmith`, `drlee`, `patient`.

### Configuration

All settings in `src/main/resources/application.properties` can be overridden with environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/hospital_db` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres1` | DB credentials |
| `JWT_SECRET` | dev key | Base64 HMAC key. **Set this anywhere other than your laptop.** |
| `JWT_VALIDITY_SECONDS` | `86400` | Token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Frontend origins (React / Vite) |
| `PORT` | `9090` | HTTP port |

## Using the API from a frontend

1. `POST /login` (or `POST /register`). The response contains `token`, `user` (with `role`) and `doctorId` / `patientId`.
2. Send `Authorization: Bearer <token>` on every later request.
3. On page reload, call `GET /me` with the stored token to restore the session.

Dates use ISO-8601 local time: `2030-01-07T09:30:00` (date-time) and `2030-01-07` (date).

Every error has the same JSON shape:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "Validation failed",
  "path": "/appointments", "fieldErrors": { "doctorId": "doctorId is required" } }
```

`fieldErrors` only appears for validation failures. Status codes: 400 invalid input, 401 missing/invalid token or bad login,
403 wrong role / not your resource, 404 not found, 409 conflict (username taken, slot already booked, duplicate email).

## Endpoints

### Auth

| Method | Path | Who | Notes |
| --- | --- | --- | --- |
| POST | `/login` | public | `{username, password}` → `{token, user, doctorId?, patientId?}` |
| POST | `/register` | public | `{username, password, confirmPassword, firstName?, lastName?, email?, phone?}`. Always creates a **patient**. Returns 201 with a token. |
| GET | `/me` | signed in | Current user + linked `doctorId` / `patientId` |

### Doctors

| Method | Path | Who | Notes |
| --- | --- | --- | --- |
| GET | `/doctors?specialty=` | public | Optional case-insensitive partial match |
| GET | `/doctors/specialties` | public | Distinct list for dropdowns |
| GET | `/doctors/{id}` | public | |
| GET | `/doctors/{id}/slots?date=2030-01-07` | public | Free 30-minute start times that day |
| GET | `/doctors/{id}/availability?dateTime=2030-01-07T09:30:00` | public | `{available: true/false}` |
| GET | `/doctors/{id}/appointments?date=&status=` | admin, that doctor | |
| POST | `/doctors` | admin | `{username, password, firstName, lastName, specialty?, phone?, availableDays?: ["MONDAY"], availableTime?: "09:00-12:00,13:00-17:00"}` |
| PUT | `/doctors/{id}` | admin, that doctor | Same profile fields, without username/password |
| DELETE | `/doctors/{id}` | admin | Also deletes the login and their appointments |

`availableDays` empty = works every day; `availableTime` empty = 09:00-17:00.

### Patients

| Method | Path | Who | Notes |
| --- | --- | --- | --- |
| GET | `/patients` | admin, doctor | |
| GET | `/patients/{id}` | admin, doctor, that patient | |
| PUT | `/patients/{id}` | admin, that patient | `{firstName, lastName, insuranceNumber, phone, email}` |
| DELETE | `/patients/{id}` | admin | Also deletes the login and their appointments |
| GET | `/patients/{id}/appointments?date=&status=` | admin, that patient, doctor (only their own bookings with that patient) | |

### Appointments

| Method | Path | Who | Notes |
| --- | --- | --- | --- |
| GET | `/appointments?doctorId=&patientId=&date=&status=` | signed in | Doctors are limited to their own schedule, patients to their own bookings |
| GET | `/appointments/{id}` | admin, its doctor, its patient | |
| POST | `/appointments` | admin, patient | `{doctorId, appointmentDate, reason?}`. Admins also pass `patientId`. |
| PUT | `/appointments/{id}` | admin, its patient | Reschedule: `{doctorId, appointmentDate, reason?}` |
| PATCH | `/appointments/{id}/status` | see below | `{status: "SCHEDULED" | "COMPLETED" | "CANCELLED"}` |
| DELETE | `/appointments/{id}` | admin | Hard delete (prefer cancelling) |

Booking rules: the time must be in the future, start on a 30-minute boundary, fall inside the doctor's
working days/hours, and not clash with another non-cancelled appointment (409).
Status changes: patients may cancel their own scheduled appointment; doctors may complete or cancel theirs;
admins may set any status.

Each appointment includes `doctorName`, `doctorSpecialty` and `patientName` for display.

### Users

| Method | Path | Who |
| --- | --- | --- |
| GET | `/users` | admin |

## Tests

```bash
./mvnw test
```

* **DAO integration tests** (`com.techelevator.dao`) need a local PostgreSQL. They create and drop a temporary
  `final_capstone_test` database. Set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` to use an existing one.
  Test data: `src/test/resources/test-data.sql`.
* **Service unit tests** (`com.techelevator.service`) cover the booking and scheduling rules with a fixed clock.
* **Controller tests** (`com.techelevator.controller`) run the real security config (JWT, CORS, role rules,
  error handler) against mocked DAOs.

## Project layout

```
controller/  REST endpoints + GlobalExceptionHandler
service/     AppointmentService (booking rules), AccountService (account + profile creation), DoctorSchedule
dao/         JdbcTemplate data access
model/       Entities and request/response DTOs
security/    JWT filter, Spring Security config, CurrentUserService
database/    schema.sql, data.sql, create.sh
```
