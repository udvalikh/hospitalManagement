package com.techelevator.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Body for booking an appointment. {@code patientId} is only honoured for admins;
 * a patient always books for themselves.
 */
public class AppointmentRequestDto {
    @NotNull(message = "doctorId is required")
    private Integer doctorId;
    private Integer patientId;
    @NotNull(message = "appointmentDate is required")
    private LocalDateTime appointmentDate;
    @Size(max = 1000, message = "reason must be at most 1000 characters")
    private String reason;

    public AppointmentRequestDto() {
    }

    public AppointmentRequestDto(Integer doctorId, Integer patientId, LocalDateTime appointmentDate, String reason) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
    }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
