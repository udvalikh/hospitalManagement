package com.techelevator.security;

import com.techelevator.model.Authority;
import com.techelevator.model.User;
import com.techelevator.service.AccountService;

/** The authenticated user plus the id of their linked doctor/patient profile (if any). */
public record CurrentUser(User user, Integer doctorId, Integer patientId) {

    public boolean hasRole(String role) {
        return user.getAuthorities().contains(new Authority(role));
    }

    public boolean isAdmin() {
        return hasRole(AccountService.ROLE_ADMIN);
    }

    public boolean isDoctor() {
        return hasRole(AccountService.ROLE_DOCTOR);
    }

    public boolean isPatient() {
        return hasRole(AccountService.ROLE_PATIENT);
    }

    public boolean isDoctor(int doctorId) {
        return this.doctorId != null && this.doctorId == doctorId;
    }

    public boolean isPatient(int patientId) {
        return this.patientId != null && this.patientId == patientId;
    }
}
