package com.techelevator.security;

import com.techelevator.dao.DoctorDao;
import com.techelevator.dao.PatientDao;
import com.techelevator.dao.UserDao;
import com.techelevator.model.Doctor;
import com.techelevator.model.Patient;
import com.techelevator.model.User;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class CurrentUserService {

    private final UserDao userDao;
    private final DoctorDao doctorDao;
    private final PatientDao patientDao;

    public CurrentUserService(UserDao userDao, DoctorDao doctorDao, PatientDao patientDao) {
        this.userDao = userDao;
        this.doctorDao = doctorDao;
        this.patientDao = patientDao;
    }

    /** Resolves the caller from the JWT principal; responds 401 if the account no longer exists. */
    public CurrentUser get(Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        return forUsername(principal.getName());
    }

    public CurrentUser forUsername(String username) {
        User user = userDao.getUserByUsername(username);
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("User account no longer exists");
        }
        CurrentUser roles = new CurrentUser(user, null, null);
        Integer doctorId = null;
        Integer patientId = null;
        if (roles.isDoctor()) {
            Doctor doctor = doctorDao.getDoctorByUserId(user.getUserId());
            doctorId = doctor == null ? null : doctor.getDoctorId();
        }
        if (roles.isPatient()) {
            Patient patient = patientDao.getPatientByUserId(user.getUserId());
            patientId = patient == null ? null : patient.getPatientId();
        }
        return new CurrentUser(user, doctorId, patientId);
    }
}
