package com.techelevator.controller;

import com.techelevator.dao.AppointmentDao;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentUpdateDto;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentController {
    private AppointmentDao appointmentDao;

    public AppointmentController(AppointmentDao appointmentDao) {
        this.appointmentDao = appointmentDao;
    }

    @GetMapping("/appointments")
    public List<Appointment> getAllAppointments() {
        return appointmentDao.getAllAppointments();
    }

    @GetMapping("/appointments/{id}")
    public Appointment getAppointmentById(@PathVariable int id) {
        return appointmentDao.getAppointmentById(id);
    }

    @GetMapping("/appointments/doctor/{doctorId}")
    public List<Appointment> getAppointmentsByDoctorId(@PathVariable int doctorId) {
        return appointmentDao.getAppointmentsByDoctorId(doctorId);
    }

    @GetMapping("/appointments/patient/{patientId}")
    public List<Appointment> getAppointmentsByPatientId(@PathVariable int patientId) {
        return appointmentDao.getAppointmentsByPatientId(patientId);
    }

    @GetMapping("/appointments/date/{date}")
    public List<Appointment> getAppointmentsByDate(@PathVariable String date) {
        return appointmentDao.getAppointmentsByDate(LocalDateTime.parse(date));
    }

    @GetMapping("/doctor-availability/{doctorId}/{date}")
    public boolean isDoctorAvailable(@PathVariable int doctorId, @PathVariable String date) {
        return appointmentDao.isDoctorAvailable(doctorId, LocalDateTime.parse(date));
    }


    @PostMapping
    public Appointment createAppointment(@RequestBody AppointmentRequestDto dto){
        return appointmentDao.createAppointment(dto);
    }
    
    @PutMapping("/{id}")
    public Appointment updateAppointment(@PathVariable int id, @RequestBody AppointmentUpdateDto dto){
        dto.setId(id);
        return appointmentDao.updateAppointment(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable int id) {
        appointmentDao.deleteAppointment(id);
    }


}
