BEGIN TRANSACTION;

-- All demo accounts use the password: password
INSERT INTO users (username, password_hash, role) VALUES ('admin',   '$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC', 'ROLE_ADMIN');
INSERT INTO users (username, password_hash, role) VALUES ('drsmith', '$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC', 'ROLE_DOCTOR');
INSERT INTO users (username, password_hash, role) VALUES ('drlee',   '$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC', 'ROLE_DOCTOR');
INSERT INTO users (username, password_hash, role) VALUES ('patient', '$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC', 'ROLE_PATIENT');

INSERT INTO doctors (user_id, first_name, last_name, specialty, phone, available_days, available_time)
VALUES ((SELECT user_id FROM users WHERE username = 'drsmith'), 'John', 'Smith', 'Cardiology', '555-0101',
        'MONDAY,WEDNESDAY,FRIDAY', '09:00-12:00,13:00-17:00');
INSERT INTO doctors (user_id, first_name, last_name, specialty, phone, available_days, available_time)
VALUES ((SELECT user_id FROM users WHERE username = 'drlee'), 'Grace', 'Lee', 'Pediatrics', '555-0102',
        'TUESDAY,THURSDAY', '08:00-16:00');

INSERT INTO patients (user_id, first_name, last_name, insurance_number, phone, email)
VALUES ((SELECT user_id FROM users WHERE username = 'patient'), 'Jane', 'Doe', 'INS-1001', '555-0201', 'jane.doe@example.com');

-- A demo booking next Monday at 10:00 with Dr. Smith (who works Mondays).
INSERT INTO appointments (doctor_id, patient_id, appointment_date, reason)
VALUES ((SELECT doctor_id FROM doctors WHERE last_name = 'Smith'),
        (SELECT patient_id FROM patients WHERE email = 'jane.doe@example.com'),
        (date_trunc('week', CURRENT_DATE) + INTERVAL '7 days 10 hours')::timestamp,
        'Annual heart checkup');

COMMIT TRANSACTION;
