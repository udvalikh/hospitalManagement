package com.techelevator.controller;

import com.techelevator.dao.AppointmentDao;
import com.techelevator.dao.DoctorDao;
import com.techelevator.model.Appointment;
import com.techelevator.model.Doctor;
import com.techelevator.model.DoctorDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class DoctorController {

    private DoctorDao doctorDao;
    private AppointmentDao appointmentDao;

    public DoctorController(DoctorDao doctorDao, AppointmentDao appointmentDao) {
        this.doctorDao = doctorDao;
        this.appointmentDao = appointmentDao;
    }

    @GetMapping("/doctors")
    public List<Doctor> getAllDoctors() {

        return doctorDao.getAllDoctors();
    }

    @GetMapping("/doctors/specialty/{specialty}")
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        return doctorDao.getDoctorsBySpecialty(specialty);
    }

    @GetMapping("/doctors/{id}")
    public Doctor getDoctorById(int id) {
        return doctorDao.getDoctorById(id);
    }

    @GetMapping("/doctors/appointments/{doctorId}")
    public List<Appointment> getAppointmentsByDoctorId(int doctorId) {
        return appointmentDao.getAppointmentsByDoctorId(doctorId);
    }

    @PostMapping("/doctors")
    public Doctor createDoctor(DoctorDto doctor) {
        return doctorDao.createDoctor(doctor);
    }

    @PutMapping("/doctors/{id}")
    public Doctor updateDoctor(@PathVariable Integer id, @RequestBody DoctorDto doctor) {
        doctor.setId(id);
        return doctorDao.updateDoctor(doctor);
    }



}
