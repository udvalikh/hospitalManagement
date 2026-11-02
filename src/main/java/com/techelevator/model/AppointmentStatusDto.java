package com.techelevator.model;

import jakarta.validation.constraints.NotNull;

public class AppointmentStatusDto {
    @NotNull(message = "status is required (SCHEDULED, COMPLETED or CANCELLED)")
    private AppointmentStatus status;

    public AppointmentStatusDto() {
    }

    public AppointmentStatusDto(AppointmentStatus status) {
        this.status = status;
    }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
