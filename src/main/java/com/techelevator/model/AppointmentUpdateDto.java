package com.techelevator.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Body for rescheduling an appointment (change doctor, time or reason). */
public class AppointmentUpdateDto {
    @NotNull(message = "doctorId is required")
    private Integer doctorId;
    @NotNull(message = "appointmentDate is required")
    private LocalDateTime appointmentDate;
    @Size(max = 1000, message = "reason must be at most 1000 characters")
    private String reason;

    public AppointmentUpdateDto() {
    }

    public AppointmentUpdateDto(Integer doctorId, LocalDateTime appointmentDate, String reason) {
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
    }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
