# Smart Hospital and Patient Portal

A production-oriented, end-to-end **Hospital / Patient Management System** that unifies patient care, clinical workflows, appointments, medical records, prescriptions, and billing in a single cohesive application.

---

## Overview

**Smart Hospital and Patient Portal** is a full-stack web application designed for hospitals, clinics, and outpatient centers. It connects patients, doctors, reception staff, and administrators through role-based access, real-time updates, and a clean, minimalist interface.

The system covers the complete care journey:

- Patients register, find doctors, book appointments, view medical history, prescriptions, and pay bills.
- Doctors manage daily schedules, access patient records, issue prescriptions, and complete visits.
- Receptionists and admins oversee appointments, invoices, insurance claim status, and system users.

Authentication is **stateless JWT** with role-based authorization (`PATIENT`, `DOCTOR`, `ADMIN`, `RECEPTIONIST`). Sensitive operations are protected on both the API and the UI. Appointments are pushed in near real time over **WebSockets**. The database schema is normalized to **3NF**, with audit columns and optimistic locking where concurrent writes are likely.

The application is **container-ready** (Docker Compose) and can also be run locally with PostgreSQL, Maven, and Node.js.

---

## Stack

| Layer | Technology | Notes |
|-------|------------|--------|
| **Backend** | Java 21, Spring Boot 3.2+ | REST APIs, validation, Actuator |
| **Security** | Spring Security + JWT (JJWT) | Access + refresh tokens, role-based method security |
| **Persistence** | Spring Data JPA, Hibernate | `ddl-auto: validate` |
| **Migrations** | Flyway | Versioned SQL under `db/migration` |
| **Database** | PostgreSQL 16 | Primary store |
| **Real-time** | Spring WebSocket (STOMP over SockJS) | Appointment book/update notifications |
| **Email** | Spring Mail (optional) | Password-reset links when SMTP is configured |
| **API docs** | springdoc-openapi (Swagger UI) | `/swagger-ui.html` |
| **Frontend** | React 18, Vite 5 | SPA with client-side routing |
| **UI** | Tailwind CSS 3 | Minimalist theme: generous whitespace, restrained palette |
| **HTTP client** | Axios | Interceptors for JWT attach & refresh |
| **Containers** | Docker, docker-compose | `postgres`, `backend`, `frontend` |

---

## Features

### 1. Authentication and account lifecycle
- **Login** — email/password with client- and server-side validation.
- **Registration** — self-register as **Patient** or **Doctor** (extra fields for specialty/license or demographics).
- **Forgot / Reset password** — time-limited reset token; email when SMTP is set, otherwise token logged for development.
- **Change password** and **update profile** (name, email, phone) from Account Settings.
- **Sign out** — revokes refresh token server-side.
- **Protected routes** — unauthenticated users are redirected to Login; API returns 401/403 as appropriate.

### 2. Role-aware home dashboard
- Summary cards: total patients/doctors, today's appointments, upcoming visits, outstanding bills (by role).
- Recent or today's appointment list with status badges.
- Quick actions for patients (book appointment, find doctors).

### 3. Doctor directory and search
- List all doctors with specialty, experience, department, consultation fee, and availability flag.
- Filter by **specialty** and **available only**.
- Deep-link into appointment booking for a selected doctor.

### 4. Appointment booking
- Patient selects doctor, date, and an **available time slot** (generated from doctor hours / weekly availability, excluding booked slots).
- Conflict and capacity checks (max patients per day).
- Confirmation screen; WebSocket notification to doctor/patient topics.
- Patients and doctors can view their appointments and update status (confirm, complete, cancel).

### 5. Patient medical records
- Visit history: chief complaint, diagnosis (incl. ICD-style code), symptoms, vitals, exam notes, treatment plan, follow-up.
- Lab results table (test name, value, reference range, abnormal flag).
- Doctors/admins can create new records tied to a patient (and optionally an appointment).

### 6. Prescriptions and treatment
- Itemized prescriptions: medication, dosage, frequency, duration, quantity, route, instructions.
- Patients see their active/past prescriptions; doctors issue new ones linked to a patient/medical record.

### 7. Doctor dashboard
- Daily schedule by date picker.
- Patient list with time slots, reason, status, and quick link to records.

### 8. Billing and insurance
- Itemized invoices (description, qty, unit price, service code).
- Tax, discount, totals, amount paid, status (`PENDING`, `PARTIALLY_PAID`, `PAID`, etc.).
- Insurance provider/policy and **claim status** (submit to review to approved/rejected/paid).
- Record payments (card, cash, UPI, bank transfer) and auto-update bill status.

### 9. User profile and settings
- Edit name, email, phone.
- Change password with current-password check.
- Display role and linked patient/doctor IDs.
- Sign out from the same page.

### 10. Platform capabilities
- **Global layout** — responsive sidebar + header, menu filtered by role.
- **Loading / empty / error states** on every major page.
- **Optimistic locking** (`version` column) on entities subject to concurrent updates.
- **Audit columns** — `created_at`, `updated_at` on all tables.
- **Swagger/OpenAPI** for interactive API exploration.

---

## API overview

Base URL (local): `http://localhost:8080`  
Frontend proxies `/api` and `/ws` to the backend in development.

### Auth — `/api/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/register` | Public | Register patient or doctor |
| POST | `/login` | Public | Login; returns access + refresh tokens + user info |
| POST | `/refresh` | Public | Exchange refresh token for new pair |
| POST | `/forgot-password` | Public | Request reset token / email |
| POST | `/reset-password` | Public | Set new password with token |
| POST | `/change-password` | JWT | Change password while logged in |
| PUT | `/profile` | JWT | Update name/email/phone |
| GET | `/me` | JWT | Current user profile |
| POST | `/logout` | JWT | Revoke refresh token |

### Doctors — `/api/doctors`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | JWT | Search; query: `specialty`, `availableOnly` |
| GET | `/{id}` | JWT | Doctor detail |
| GET | `/{id}/slots` | JWT | Available slots for `date` (ISO date) |
| GET | `/specialties` | JWT | List specialty enum values |

### Appointments — `/api/appointments`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/` | JWT (Patient) | Book appointment |
| GET | `/me` | JWT | My appointments (patient or doctor) |
| GET | `/schedule` | JWT (Doctor) | Schedule for optional `date` |
| GET | `/{id}` | JWT | Appointment detail (access-checked) |
| PATCH | `/{id}/status` | JWT | Update status + optional notes |

### Medical records — `/api/medical-records`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/patient/{patientId}` | JWT | List records for patient |
| GET | `/{id}` | JWT | Single record |
| POST | `/` | JWT (Doctor/Admin) | Create record |
| GET | `/patient/{patientId}/lab-results` | JWT | Lab results for patient |

### Prescriptions — `/api/prescriptions`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/patient/{patientId}` | JWT | List prescriptions |
| GET | `/{id}` | JWT | Prescription with items |
| POST | `/` | JWT (Doctor/Admin) | Issue prescription |

### Billing — `/api/billings`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/patient/{patientId}` | JWT | List invoices |
| GET | `/{id}` | JWT | Invoice with items and payments |
| POST | `/` | JWT (Staff) | Create invoice |
| POST | `/{id}/payments` | JWT | Record payment |
| PATCH | `/{id}/claim` | JWT | Update insurance claim fields |

### Dashboard — `/api/dashboard`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | JWT | Role-aware stats and appointment summaries |

### Real-time — WebSocket

| Endpoint | Protocol | Topics (examples) |
|----------|----------|-------------------|
| `/ws` | STOMP over SockJS | `/topic/appointments`, `/topic/doctor/{id}/appointments`, `/topic/patient/{id}/appointments` |

### Docs and health

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/api-docs`
- Health: `/actuator/health`

---

## Database schema

Schema is defined in Flyway migration `V1__init_schema.sql` and validated by JPA. Design goals: **3NF**, referential integrity, audit columns, and optimistic locking.

### Entity relationship (logical)

```
users 1--1 patients
users 1--1 doctors
doctors 1--* doctor_availability
patients 1--* appointments *--1 doctors
patients 1--* medical_records *--1 doctors
appointments 0..1--0..1 medical_records
patients 1--* lab_results (optional link to medical_records)
patients 1--* prescriptions *--1 doctors
prescriptions 1--* prescription_items
patients 1--* billings (optional link to appointments)
billings 1--* billing_items
billings 1--* payments
users 1--* refresh_tokens
```

### Core tables

| Table | Purpose | Notable columns |
|-------|---------|-----------------|
| **users** | Auth and shared profile | `email` (unique), `password_hash`, `role`, `reset_token`, `is_active`, `version` |
| **patients** | Patient demographics and insurance | `user_id` (FK unique), DOB, gender, blood group, allergies, policy number |
| **doctors** | Clinical profile | `user_id` (FK unique), `specialty`, `license_number`, fees, hours, `is_available` |
| **doctor_availability** | Weekly slots | `day_of_week`, `start_time`, `end_time`, `slot_duration_minutes` |
| **appointments** | Bookings | patient/doctor FKs, date, start/end time, `status`; unique slot index excluding cancelled |
| **medical_records** | Visit notes | diagnosis, ICD-style code, vitals, treatment plan, optional `appointment_id` |
| **lab_results** | Diagnostics | test name/code, value, unit, reference range, `is_abnormal` |
| **prescriptions** | Rx header | date, valid until, notes, `is_active` |
| **prescription_items** | Rx lines | medication, dosage, frequency, duration, route |
| **billings** | Invoices | invoice number, amounts, `status`, insurance and `claim_status` |
| **billing_items** | Line items | description, qty, unit/total price, service code |
| **payments** | Collections | amount, method, transaction id, `received_by` |
| **refresh_tokens** | JWT refresh | token, expiry, `revoked` |

Every business table includes:

- `id` — `BIGSERIAL` primary key
- `created_at` / `updated_at` — timestamptz
- `version` — bigint for optimistic locking

Enums are stored as strings (e.g. role, specialty, appointment status, billing status, claim status) with CHECK constraints where applicable.

Seed users and sample doctors/patients are created by `DataInitializer` on first boot (not hard-coded weak hashes in SQL).

---

## Project structure

```
hospital-management-system/
|
|-- README.md
|-- docker-compose.yml          # postgres + backend + frontend
|-- .gitignore
|
|-- backend/                    # Smart Hospital API (Spring Boot)
|   |-- Dockerfile
|   |-- pom.xml
|   |-- src/
|       |-- main/
|       |   |-- java/com/hospital/
|       |   |   |-- HospitalManagementApplication.java
|       |   |   |-- config/           # Security, WebSocket, DataInitializer
|       |   |   |-- controller/       # REST controllers (auth, doctors, appointments, ...)
|       |   |   |-- dto/              # Request/response DTOs + validation
|       |   |   |-- entity/           # JPA entities (User, Patient, Doctor, ...)
|       |   |   |-- enums/            # Role, Specialty, statuses, ...
|       |   |   |-- exception/        # Global handler + domain exceptions
|       |   |   |-- repository/       # Spring Data JPA repositories
|       |   |   |-- security/         # JWT service, filter, UserDetailsService
|       |   |   |-- service/          # Business logic
|       |   |   |-- websocket/        # Notification publisher
|       |   |-- resources/
|       |       |-- application.yml
|       |       |-- db/migration/
|       |           |-- V1__init_schema.sql
|       |           |-- V2__seed_data.sql   # placeholder; real seed via DataInitializer
|       |-- test/
|
|-- frontend/                   # Smart Hospital Patient Portal (React)
    |-- Dockerfile
    |-- nginx.conf              # SPA + /api and /ws proxy in container
    |-- package.json
    |-- vite.config.js
    |-- tailwind.config.js
    |-- postcss.config.js
    |-- index.html
    |-- src/
        |-- main.jsx
        |-- App.jsx               # Routes + auth guards
        |-- index.css             # Tailwind + shared component classes
        |-- context/
        |   |-- AuthContext.jsx   # Login, register, logout, token storage
        |-- services/
        |   |-- api.js            # Axios instance + refresh interceptor
        |-- layouts/
        |   |-- AppLayout.jsx     # Sidebar + header shell
        |-- pages/
        |   |-- LoginPage.jsx
        |   |-- RegisterPage.jsx
        |   |-- ForgotPasswordPage.jsx
        |   |-- ResetPasswordPage.jsx
        |   |-- DashboardPage.jsx
        |   |-- DoctorsPage.jsx
        |   |-- BookAppointmentPage.jsx
        |   |-- AppointmentsPage.jsx
        |   |-- MedicalRecordsPage.jsx
        |   |-- PrescriptionsPage.jsx
        |   |-- DoctorDashboardPage.jsx
        |   |-- BillingPage.jsx
        |   |-- ProfilePage.jsx
        |-- components/           # Shared UI (extend as needed)
        |-- hooks/
        |-- utils/
```

---

## Getting started

### Docker (recommended)

```bash
cd hospital-management-system
docker compose up --build
```

| Service | URL |
|---------|-----|
| Patient Portal (UI) | http://localhost:5173 |
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 (`hospital` / `hospital123` / `hospital_db`) |

Ensure **Docker Desktop is running** before `docker compose up`.

### Local development (no Docker for app)

1. Start **PostgreSQL 16** and create database/user as above.
2. **Backend** (JDK 21 + Maven):

```bash
cd backend
# set DB_* / JWT_SECRET / FRONTEND_URL / CORS_ORIGINS as needed
mvn spring-boot:run
```

3. **Frontend** (Node 20+):

```bash
cd frontend
npm install
npm run dev
```

Vite proxies `/api` and `/ws` to `http://localhost:8080`.

### Demo accounts

Password for all seeded users: **`Password@123`**

| Email | Role |
|-------|------|
| `admin@hospital.com` | ADMIN |
| `dr.smith@hospital.com` | DOCTOR (Cardiology) |
| `dr.jones@hospital.com` | DOCTOR (Pediatrics) |
| `dr.patel@hospital.com` | DOCTOR (General Practice) |
| `patient1@example.com` | PATIENT |
| `patient2@example.com` | PATIENT |
| `reception@hospital.com` | RECEPTIONIST |

---

## Environment variables

### Backend

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | Port |
| `DB_NAME` | `hospital_db` | Database name |
| `DB_USER` | `hospital` | User |
| `DB_PASSWORD` | `hospital123` | Password |
| `JWT_SECRET` | (long default in yml) | HMAC secret (256-bit recommended) |
| `FRONTEND_URL` | `http://localhost:5173` | Used in reset-password links |
| `CORS_ORIGINS` | `http://localhost:5173,...` | Allowed origins |
| `SERVER_PORT` | `8080` | HTTP port |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | empty | Optional SMTP for reset emails |

### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | `/api` | API base (use default with Vite proxy or nginx) |
