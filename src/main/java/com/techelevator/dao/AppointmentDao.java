package com.techelevator.dao;

import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentStatus;
import com.techelevator.model.AppointmentUpdateDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentDao {

    /** @return the appointment, or {@code null} if none exists. */
    Appointment getAppointmentById(int appointmentId);

    /**
     * Lists appointments ordered by date. Every filter is optional ({@code null} = no filter).
     *
     * @param date matches every appointment on that calendar day
     */
    List<Appointment> getAppointments(Integer doctorId, Integer patientId, LocalDate date, AppointmentStatus status);

    /** Requires {@code doctorId}, {@code patientId} and {@code appointmentDate} to be set. */
    Appointment createAppointment(AppointmentRequestDto appointment);

    /** @return the updated appointment, or {@code null} if none exists. */
    Appointment updateAppointment(int appointmentId, AppointmentUpdateDto appointment);

    /** @return the updated appointment, or {@code null} if none exists. */
    Appointment updateStatus(int appointmentId, AppointmentStatus status);

    boolean deleteAppointment(int appointmentId);

    /**
     * @param excludeAppointmentId ignore this appointment (used when rescheduling); may be {@code null}
     * @return true if the doctor has no non-cancelled appointment starting at {@code dateTime}
     */
    boolean isDoctorAvailable(int doctorId, LocalDateTime dateTime, Integer excludeAppointmentId);

    /** Start times of the doctor's non-cancelled appointments on the given day. */
    List<LocalDateTime> getBookedTimes(int doctorId, LocalDate date);
}
