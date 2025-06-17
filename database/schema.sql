BEGIN TRANSACTION;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
	user_id SERIAL,
	username varchar(50) NOT NULL UNIQUE,
	password_hash varchar(200) NOT NULL,
	role varchar(50) NOT NULL,
	CONSTRAINT PK_user PRIMARY KEY (user_id)
);

CREATE TABLE authorities (
    authority_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_authorities (
    user_id INT NOT NULL REFERENCES users(user_id),
    authority_id INT NOT NULL REFERENCES authorities(authority_id),
    PRIMARY KEY (user_id, authority_id)
);

CREATE TABLE doctors (
    doctor_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    bio TEXT,
    availability_notes TEXT
);

CREATE TABLE patients (
    patient_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    phone_number VARCHAR(15),
    address TEXT
);
CREATE TABLE appointments (
    appointment_id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL REFERENCES patients(patient_id),
    doctor_id INT NOT NULL REFERENCES doctors(doctor_id),
    appointment_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED', -- could be CANCELED, COMPLETED, etc.
    reason TEXT
);

CREATE TABLE doctor_availability (
    id SERIAL PRIMARY KEY,
    doctor_id INT NOT NULL REFERENCES doctors(doctor_id),
    available_from TIMESTAMP,
    available_to TIMESTAMP
);

CREATE TABLE medical_records (
    id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL REFERENCES patients(patient_id),
    doctor_id INT NOT NULL REFERENCES doctors(doctor_id),
    visit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    diagnosis TEXT,
    notes TEXT
);

CREATE TABLE prescriptions (
    id SERIAL PRIMARY KEY,
    medical_record_id INT NOT NULL REFERENCES medical_records(id),
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    instructions TEXT
);

COMMIT TRANSACTION;
