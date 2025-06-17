package com.techelevator.model;

import java.time.LocalDateTime;

public class AppointmentRequestDto {
    private int id;
    private int doctorId;
    private int patientId;
    private LocalDateTime appointmentDate;
    private String reason;


    public AppointmentRequestDto() {
    }

    public AppointmentRequestDto(int id, int doctorId, int patientId, LocalDateTime appointmentDateTime, String reason) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId =patientId;
        this.appointmentDate = appointmentDateTime;
        this.reason = reason;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
}
