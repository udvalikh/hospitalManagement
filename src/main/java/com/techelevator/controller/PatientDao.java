package com.techelevator.controller;

import com.techelevator.model.Patient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class PatientDao {

    private PatientDao patientDao;

    public PatientDao(PatientDao patientDao) {
        this.patientDao = patientDao;
    }

    @GetMapping("/patients")
    public List<Patient> getAllPatients() {
        return patientDao.getAllPatients();
    }

    @GetMapping("/patients/{id}")
    public Patient getPatientById(@PathVariable int id) {
        return patientDao.getPatientById(id);
    }

    @GetMapping("/patients/doctor/{doctorId}")
    public List<Patient> getPatientsByDoctorId(@PathVariable int doctorId) {
        return patientDao.getPatientsByDoctorId(doctorId);
    }


}
