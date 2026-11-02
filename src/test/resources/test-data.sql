BEGIN TRANSACTION;

-- user_id 1-3: plain users used by JdbcUserDaoTest
INSERT INTO users (username,password_hash,role) VALUES ('user1','user1','ROLE_USER');
INSERT INTO users (username,password_hash,role) VALUES ('user2','user2','ROLE_USER');
INSERT INTO users (username,password_hash,role) VALUES ('user3','user3','ROLE_USER');
-- user_id 4-8
INSERT INTO users (username,password_hash,role) VALUES ('doctor1','doctor1','ROLE_DOCTOR');
INSERT INTO users (username,password_hash,role) VALUES ('doctor2','doctor2','ROLE_DOCTOR');
INSERT INTO users (username,password_hash,role) VALUES ('patient1','patient1','ROLE_PATIENT');
INSERT INTO users (username,password_hash,role) VALUES ('patient2','patient2','ROLE_PATIENT');
INSERT INTO users (username,password_hash,role) VALUES ('admin','admin','ROLE_ADMIN');

-- doctor_id 1: works Mon/Wed mornings. doctor_id 2: no schedule set (every day, 09:00-17:00).
INSERT INTO doctors (user_id, first_name, last_name, specialty, phone, available_days, available_time)
VALUES (4, 'Alice', 'Adams', 'Cardiology', '555-1001', 'MONDAY,WEDNESDAY', '09:00-12:00');
INSERT INTO doctors (user_id, first_name, last_name, specialty, phone, available_days, available_time)
VALUES (5, 'Bob', 'Brown', 'Pediatrics', '555-1002', NULL, NULL);

-- patient_id 1, 2
INSERT INTO patients (user_id, first_name, last_name, insurance_number, phone, email)
VALUES (6, 'Carol', 'Clark', 'INS-1', '555-2001', 'carol@example.com');
INSERT INTO patients (user_id, first_name, last_name, insurance_number, phone, email)
VALUES (7, 'Dan', 'Davis', 'INS-2', '555-2002', 'dan@example.com');

-- appointment_id 1-4. 2030-01-07 is a Monday, 2030-01-09 a Wednesday.
INSERT INTO appointments (doctor_id, patient_id, appointment_date, status, reason)
VALUES (1, 1, '2030-01-07 09:00:00', 'SCHEDULED', 'Checkup');
INSERT INTO appointments (doctor_id, patient_id, appointment_date, status, reason)
VALUES (1, 2, '2030-01-07 10:00:00', 'SCHEDULED', 'Chest pain');
INSERT INTO appointments (doctor_id, patient_id, appointment_date, status, reason)
VALUES (2, 1, '2030-01-08 14:00:00', 'CANCELLED', 'Flu shot');
INSERT INTO appointments (doctor_id, patient_id, appointment_date, status, reason)
VALUES (1, 1, '2030-01-09 09:00:00', 'COMPLETED', 'Follow-up');

COMMIT TRANSACTION;
