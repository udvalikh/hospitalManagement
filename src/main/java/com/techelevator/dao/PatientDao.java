package com.techelevator.dao;

import com.techelevator.model.Patient;
import com.techelevator.model.PatientDto;

import java.util.List;
import java.util.Optional;

public interface PatientDao {
    Patient createPatient(PatientDto dataIn);

    Optional<Patient> findById(Long id);
    List<Patient> findAll();


    Patient updatePatient(PatientDto dataIn);


    boolean deleteById(Long id);

    Optional<Patient> findByEmail(String email);
}
