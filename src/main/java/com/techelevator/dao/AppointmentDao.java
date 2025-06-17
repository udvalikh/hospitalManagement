package com.techelevator.dao;

import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentUpdateDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentDao {
    Appointment createAppointment(AppointmentRequestDto dto);
    Appointment getAppointmentById(int id);
    List<Appointment> getAllAppointments();
    Appointment updateAppointment(AppointmentUpdateDto dto);
    boolean deleteAppointment(int id);
    List<Appointment> getAppointmentsByPatientId(int patientId);
    List<Appointment> getAppointmentsByDoctorId(int doctorId);
    List<Appointment> getAppointmentsByDate(LocalDateTime date);
    boolean isDoctorAvailable(int doctorId, LocalDateTime date);
}
