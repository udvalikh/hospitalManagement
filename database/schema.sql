BEGIN TRANSACTION;

-- Drop in dependency order so the script can be re-run safely.
DROP TABLE IF EXISTS appointments, patients, doctors, user_authorities, authorities, users CASCADE;

CREATE TABLE users (
	user_id SERIAL,
	username varchar(50) NOT NULL UNIQUE,
	password_hash varchar(200) NOT NULL,
	role varchar(50) NOT NULL, -- ROLE_ADMIN, ROLE_DOCTOR, ROLE_PATIENT
	CONSTRAINT PK_user PRIMARY KEY (user_id)
);

CREATE TABLE doctors (
    doctor_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100),
    phone VARCHAR(20),
    available_days VARCHAR(100),  -- e.g. 'MONDAY,TUESDAY,WEDNESDAY'
    available_time VARCHAR(100)   -- e.g. '09:00-12:00,13:00-17:00'
);

CREATE TABLE patients (
    patient_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    first_name VARCHAR(100),      -- nullable so a patient can self-register with just username/password
    last_name VARCHAR(100),
    insurance_number VARCHAR(50) UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE
);

CREATE TABLE appointments (
    appointment_id SERIAL PRIMARY KEY,
    doctor_id INT NOT NULL REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    patient_id INT NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE,
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
        CONSTRAINT CK_appointment_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED')),
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- A doctor can't have two active appointments that start at the same moment.
CREATE UNIQUE INDEX UX_appointments_doctor_slot
    ON appointments (doctor_id, appointment_date)
    WHERE status <> 'CANCELLED';
CREATE INDEX IX_appointments_patient ON appointments (patient_id);

--CREATE TABLE medical_records (
--    medical_record_id SERIAL PRIMARY KEY,
--    patient_id INT NOT NULL REFERENCES patients(patient_id),
--    doctor_id INT NOT NULL REFERENCES doctors(doctor_id),
--    visit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--    diagnosis TEXT,
--    notes TEXT
--);
--
--CREATE TABLE prescriptions (
--    prescription_id SERIAL PRIMARY KEY,
--    medical_record_id INT NOT NULL REFERENCES medical_records(medical_record_id),
--    medication_name VARCHAR(255) NOT NULL,
--    dosage VARCHAR(100),
--    instructions TEXT
--);

COMMIT TRANSACTION;
