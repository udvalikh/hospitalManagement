package com.techelevator.dao;

import com.techelevator.model.Doctor;
import com.techelevator.model.DoctorDto;

import java.util.List;

public interface DoctorDao {
    Doctor getDoctorById(int id);
    List<Doctor> getAllDoctors();
    List<Doctor> getDoctorsBySpecialty(String specialty);
    Doctor createDoctor(DoctorDto doctor);
    Doctor updateDoctor(DoctorDto doctor);
    boolean deleteDoctor(int id);
}
