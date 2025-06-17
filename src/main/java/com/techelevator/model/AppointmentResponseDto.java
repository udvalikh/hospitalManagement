package com.techelevator.model;

import java.time.LocalDateTime;

public class AppointmentResponseDto {
    private String doctorName;
    private String specialization;
    private LocalDateTime appointmentDateTime;
    private String status;

    public AppointmentResponseDto() {
    }

    public AppointmentResponseDto(String doctorName, String specialization, LocalDateTime appointmentDateTime, String status) {
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
