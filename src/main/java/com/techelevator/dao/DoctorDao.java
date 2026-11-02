package com.techelevator.dao;

import com.techelevator.model.Doctor;
import com.techelevator.model.DoctorDto;

import java.util.List;

public interface DoctorDao {

    /** @return the doctor, or {@code null} if none exists. */
    Doctor getDoctorById(int doctorId);

    /** @return the doctor profile linked to a user account, or {@code null}. */
    Doctor getDoctorByUserId(int userId);

    /** @param specialty optional case-insensitive partial match; {@code null} returns every doctor. */
    List<Doctor> getDoctors(String specialty);

    List<String> getSpecialties();

    Doctor createDoctor(int userId, DoctorDto doctor);

    /** @return the updated doctor, or {@code null} if none exists. */
    Doctor updateDoctor(int doctorId, DoctorDto doctor);

    /** Deletes the doctor together with their login account and appointments. */
    boolean deleteDoctor(int doctorId);
}
