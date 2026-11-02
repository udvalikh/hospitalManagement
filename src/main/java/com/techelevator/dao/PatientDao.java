package com.techelevator.dao;

import com.techelevator.model.Patient;
import com.techelevator.model.PatientDto;

import java.util.List;

public interface PatientDao {

    /** @return the patient, or {@code null} if none exists. */
    Patient getPatientById(int patientId);

    /** @return the patient profile linked to a user account, or {@code null}. */
    Patient getPatientByUserId(int userId);

    List<Patient> getPatients();

    Patient createPatient(int userId, PatientDto patient);

    /** @return the updated patient, or {@code null} if none exists. */
    Patient updatePatient(int patientId, PatientDto patient);

    /** Deletes the patient together with their login account and appointments. */
    boolean deletePatient(int patientId);
}
