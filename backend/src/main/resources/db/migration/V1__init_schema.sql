-- Hospital Management System - Initial Schema (3NF)
-- PostgreSQL 16

-- Users table (authentication & base profile)
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    role            VARCHAR(20)  NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    reset_token     VARCHAR(255),
    reset_token_expiry TIMESTAMPTZ,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT chk_users_role CHECK (role IN ('PATIENT', 'DOCTOR', 'ADMIN', 'RECEPTIONIST'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

-- Patients
CREATE TABLE patients (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    date_of_birth           DATE,
    gender                  VARCHAR(30),
    blood_group             VARCHAR(10),
    address                 TEXT,
    emergency_contact_name  VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    insurance_provider      VARCHAR(150),
    insurance_policy_number VARCHAR(100),
    allergies               TEXT,
    chronic_conditions      TEXT,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version                 BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT chk_patients_gender CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY'))
);

CREATE INDEX idx_patients_user_id ON patients (user_id);

-- Doctors
CREATE TABLE doctors (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT         NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    specialty             VARCHAR(50)    NOT NULL,
    license_number        VARCHAR(50)    NOT NULL UNIQUE,
    years_of_experience   INTEGER,
    bio                   TEXT,
    consultation_fee      NUMERIC(10, 2),
    available_from        TIME,
    available_to          TIME,
    is_available          BOOLEAN        NOT NULL DEFAULT TRUE,
    max_patients_per_day  INTEGER        DEFAULT 20,
    department            VARCHAR(100),
    qualifications        TEXT,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    version               BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_doctors_user_id ON doctors (user_id);
CREATE INDEX idx_doctors_specialty ON doctors (specialty);
CREATE INDEX idx_doctors_is_available ON doctors (is_available);

-- Doctor weekly availability slots
CREATE TABLE doctor_availability (
    id                    BIGSERIAL PRIMARY KEY,
    doctor_id             BIGINT       NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week           VARCHAR(15)  NOT NULL,
    start_time            TIME         NOT NULL,
    end_time              TIME         NOT NULL,
    slot_duration_minutes INTEGER      NOT NULL DEFAULT 30,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version               BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT chk_availability_day CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    CONSTRAINT chk_availability_times CHECK (start_time < end_time)
);

CREATE INDEX idx_doctor_availability_doctor ON doctor_availability (doctor_id);

-- Appointments
CREATE TABLE appointments (
    id                   BIGSERIAL PRIMARY KEY,
    patient_id           BIGINT       NOT NULL REFERENCES patients(id),
    doctor_id            BIGINT       NOT NULL REFERENCES doctors(id),
    appointment_date     DATE         NOT NULL,
    start_time           TIME         NOT NULL,
    end_time             TIME         NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    reason               TEXT,
    notes                TEXT,
    cancellation_reason  TEXT,
    created_by           BIGINT       REFERENCES users(id),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version              BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT chk_appointments_status CHECK (status IN ('SCHEDULED','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED','NO_SHOW')),
    CONSTRAINT chk_appointments_times CHECK (start_time < end_time)
);

CREATE INDEX idx_appointments_patient ON appointments (patient_id);
CREATE INDEX idx_appointments_doctor ON appointments (doctor_id);
CREATE INDEX idx_appointments_date ON appointments (appointment_date);
CREATE INDEX idx_appointments_status ON appointments (status);
CREATE UNIQUE INDEX uq_doctor_slot ON appointments (doctor_id, appointment_date, start_time)
    WHERE status NOT IN ('CANCELLED', 'NO_SHOW');

-- Medical Records
CREATE TABLE medical_records (
    id                  BIGSERIAL PRIMARY KEY,
    patient_id          BIGINT       NOT NULL REFERENCES patients(id),
    doctor_id           BIGINT       NOT NULL REFERENCES doctors(id),
    appointment_id      BIGINT       UNIQUE REFERENCES appointments(id),
    visit_date          DATE         NOT NULL,
    chief_complaint     TEXT,
    diagnosis           TEXT,
    diagnosis_code      VARCHAR(20),
    symptoms            TEXT,
    vital_signs         TEXT,
    examination_notes   TEXT,
    treatment_plan      TEXT,
    follow_up_date      DATE,
    follow_up_notes     TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version             BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_medical_records_patient ON medical_records (patient_id);
CREATE INDEX idx_medical_records_doctor ON medical_records (doctor_id);
CREATE INDEX idx_medical_records_appointment ON medical_records (appointment_id);

-- Lab Results
CREATE TABLE lab_results (
    id                  BIGSERIAL PRIMARY KEY,
    patient_id          BIGINT       NOT NULL REFERENCES patients(id),
    medical_record_id   BIGINT       REFERENCES medical_records(id),
    ordered_by          BIGINT       REFERENCES doctors(id),
    test_name           VARCHAR(200) NOT NULL,
    test_code           VARCHAR(50),
    result_value        VARCHAR(100),
    unit                VARCHAR(50),
    reference_range     VARCHAR(100),
    is_abnormal         BOOLEAN      DEFAULT FALSE,
    test_date           DATE         NOT NULL,
    result_date         DATE,
    notes               TEXT,
    lab_name            VARCHAR(150),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version             BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_lab_results_patient ON lab_results (patient_id);
CREATE INDEX idx_lab_results_medical_record ON lab_results (medical_record_id);

-- Prescriptions
CREATE TABLE prescriptions (
    id                  BIGSERIAL PRIMARY KEY,
    patient_id          BIGINT       NOT NULL REFERENCES patients(id),
    doctor_id           BIGINT       NOT NULL REFERENCES doctors(id),
    medical_record_id   BIGINT       REFERENCES medical_records(id),
    prescription_date   DATE         NOT NULL,
    valid_until         DATE,
    notes               TEXT,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version             BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_prescriptions_patient ON prescriptions (patient_id);
CREATE INDEX idx_prescriptions_doctor ON prescriptions (doctor_id);
CREATE INDEX idx_prescriptions_medical_record ON prescriptions (medical_record_id);

-- Prescription Items
CREATE TABLE prescription_items (
    id                  BIGSERIAL PRIMARY KEY,
    prescription_id     BIGINT       NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medication_name     VARCHAR(200) NOT NULL,
    dosage              VARCHAR(100) NOT NULL,
    frequency           VARCHAR(100) NOT NULL,
    duration            VARCHAR(100),
    quantity            INTEGER,
    instructions        TEXT,
    route               VARCHAR(50),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version             BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_prescription_items_prescription ON prescription_items (prescription_id);

-- Billings
CREATE TABLE billings (
    id                      BIGSERIAL PRIMARY KEY,
    patient_id              BIGINT         NOT NULL REFERENCES patients(id),
    appointment_id          BIGINT         REFERENCES appointments(id),
    invoice_number          VARCHAR(50)    NOT NULL UNIQUE,
    billing_date            DATE           NOT NULL,
    due_date                DATE,
    subtotal                NUMERIC(12, 2) NOT NULL,
    tax_amount              NUMERIC(12, 2) DEFAULT 0,
    discount_amount         NUMERIC(12, 2) DEFAULT 0,
    total_amount            NUMERIC(12, 2) NOT NULL,
    amount_paid             NUMERIC(12, 2) DEFAULT 0,
    status                  VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    insurance_provider      VARCHAR(150),
    insurance_policy_number VARCHAR(100),
    claim_status            VARCHAR(30)    DEFAULT 'NOT_SUBMITTED',
    claim_number            VARCHAR(100),
    claim_amount            NUMERIC(12, 2),
    notes                   TEXT,
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    version                 BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT chk_billings_status CHECK (status IN ('PENDING','PARTIALLY_PAID','PAID','OVERDUE','CANCELLED','REFUNDED')),
    CONSTRAINT chk_claim_status CHECK (claim_status IN ('NOT_SUBMITTED','SUBMITTED','UNDER_REVIEW','APPROVED','PARTIALLY_APPROVED','REJECTED','PAID'))
);

CREATE INDEX idx_billings_patient ON billings (patient_id);
CREATE INDEX idx_billings_status ON billings (status);
CREATE INDEX idx_billings_invoice ON billings (invoice_number);

-- Billing Items
CREATE TABLE billing_items (
    id              BIGSERIAL PRIMARY KEY,
    billing_id      BIGINT         NOT NULL REFERENCES billings(id) ON DELETE CASCADE,
    description     VARCHAR(300)   NOT NULL,
    quantity        INTEGER        NOT NULL DEFAULT 1,
    unit_price      NUMERIC(12, 2) NOT NULL,
    total_price     NUMERIC(12, 2) NOT NULL,
    service_code    VARCHAR(50),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    version         BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_billing_items_billing ON billing_items (billing_id);

-- Payments
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    billing_id      BIGINT         NOT NULL REFERENCES billings(id) ON DELETE CASCADE,
    amount          NUMERIC(12, 2) NOT NULL,
    payment_method  VARCHAR(50)    NOT NULL,
    transaction_id  VARCHAR(100),
    payment_date    TIMESTAMPTZ    NOT NULL,
    notes           TEXT,
    received_by     BIGINT         REFERENCES users(id),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    version         BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_payments_billing ON payments (billing_id);

-- Refresh tokens (for JWT refresh)
CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token           VARCHAR(512) NOT NULL UNIQUE,
    expiry_date     TIMESTAMPTZ  NOT NULL,
    revoked         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
