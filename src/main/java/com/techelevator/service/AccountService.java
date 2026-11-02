package com.techelevator.service;

import com.techelevator.dao.DoctorDao;
import com.techelevator.dao.PatientDao;
import com.techelevator.dao.UserDao;
import com.techelevator.exception.BadRequestException;
import com.techelevator.exception.ConflictException;
import com.techelevator.model.CreateDoctorDto;
import com.techelevator.model.Doctor;
import com.techelevator.model.Patient;
import com.techelevator.model.PatientDto;
import com.techelevator.model.RegisterUserDto;
import com.techelevator.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates a login account and its linked profile in one transaction. */
@Service
public class AccountService {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_DOCTOR = "ROLE_DOCTOR";
    public static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final UserDao userDao;
    private final DoctorDao doctorDao;
    private final PatientDao patientDao;

    public AccountService(UserDao userDao, DoctorDao doctorDao, PatientDao patientDao) {
        this.userDao = userDao;
        this.doctorDao = doctorDao;
        this.patientDao = patientDao;
    }

    /** Public sign-up. Always creates a patient, whatever role the client asked for. */
    @Transactional
    public Patient registerPatient(RegisterUserDto registration) {
        if (!registration.getPassword().equals(registration.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }
        User user = createUser(registration.getUsername(), registration.getPassword(), ROLE_PATIENT);
        PatientDto profile = new PatientDto(registration.getFirstName(), registration.getLastName(), null,
                registration.getPhone(), registration.getEmail());
        return patientDao.createPatient(user.getUserId(), profile);
    }

    @Transactional
    public Doctor createDoctor(CreateDoctorDto request) {
        User user = createUser(request.getUsername(), request.getPassword(), ROLE_DOCTOR);
        return doctorDao.createDoctor(user.getUserId(), request);
    }

    private User createUser(String username, String password, String role) {
        if (userDao.getUserByUsername(username.trim()) != null) {
            throw new ConflictException("Username '" + username.trim() + "' is already taken");
        }
        RegisterUserDto account = new RegisterUserDto();
        account.setUsername(username);
        account.setPassword(password);
        account.setRole(role);
        return userDao.createUser(account);
    }
}
