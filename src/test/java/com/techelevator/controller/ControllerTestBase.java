package com.techelevator.controller;

import com.techelevator.dao.AppointmentDao;
import com.techelevator.dao.DoctorDao;
import com.techelevator.dao.PatientDao;
import com.techelevator.dao.UserDao;
import com.techelevator.model.Doctor;
import com.techelevator.model.Patient;
import com.techelevator.model.User;
import com.techelevator.security.CurrentUserService;
import com.techelevator.security.JwtAccessDeniedHandler;
import com.techelevator.security.JwtAuthenticationEntryPoint;
import com.techelevator.security.UserModelDetailsService;
import com.techelevator.security.WebSecurityConfig;
import com.techelevator.security.jwt.TokenProvider;
import com.techelevator.service.AccountService;
import com.techelevator.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

/**
 * Web-layer tests with the real security config (JWT filter, CORS, role rules, error handler) and mocked DAOs/services.
 * Accounts set up for every test:
 * admin, doc (doctorId 1), doc2 (doctorId 2), pat (patientId 5), pat2 (patientId 6).
 */
@WebMvcTest
@Import({WebSecurityConfig.class, TokenProvider.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class,
        UserModelDetailsService.class, CurrentUserService.class})
abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected UserDao userDao;
    @MockBean
    protected DoctorDao doctorDao;
    @MockBean
    protected PatientDao patientDao;
    @MockBean
    protected AppointmentDao appointmentDao;
    @MockBean
    protected AppointmentService appointmentService;
    @MockBean
    protected AccountService accountService;

    @BeforeEach
    void setupAccounts() {
        givenAccount(1, "admin", "ROLE_ADMIN", null, null);
        givenAccount(2, "doc", "ROLE_DOCTOR", 1, null);
        givenAccount(3, "doc2", "ROLE_DOCTOR", 2, null);
        givenAccount(4, "pat", "ROLE_PATIENT", null, 5);
        givenAccount(5, "pat2", "ROLE_PATIENT", null, 6);
    }

    protected User givenAccount(int userId, String username, String role, Integer doctorId, Integer patientId) {
        // "password" hashed with BCrypt (same as database/data.sql)
        User user = new User(userId, username, "$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC", role);
        when(userDao.getUserByUsername(username)).thenReturn(user);
        if (doctorId != null) {
            Doctor doctor = doctor(doctorId);
            doctor.setUser(user);
            when(doctorDao.getDoctorByUserId(userId)).thenReturn(doctor);
            when(doctorDao.getDoctorById(doctorId)).thenReturn(doctor);
        }
        if (patientId != null) {
            Patient patient = new Patient(patientId, user, "First" + patientId, "Last" + patientId, null, null, null);
            when(patientDao.getPatientByUserId(userId)).thenReturn(patient);
            when(patientDao.getPatientById(patientId)).thenReturn(patient);
        }
        return user;
    }

    protected static Doctor doctor(int doctorId) {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(doctorId);
        doctor.setFirstName("Doc" + doctorId);
        doctor.setLastName("Tor");
        doctor.setSpecialty("Cardiology");
        return doctor;
    }
}
