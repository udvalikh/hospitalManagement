package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentStatus;
import com.techelevator.model.AppointmentUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcAppointmentDaoTest extends BaseDaoTest {

    private static final LocalDateTime MONDAY_9AM = LocalDateTime.of(2030, 1, 7, 9, 0);
    private static final LocalDate MONDAY = MONDAY_9AM.toLocalDate();

    private JdbcAppointmentDao sut;

    @BeforeEach
    public void setup() {
        sut = new JdbcAppointmentDao(new JdbcTemplate(dataSource));
    }

    @Test
    public void getAppointmentById_returns_appointment_with_display_names() {
        Appointment appointment = sut.getAppointmentById(1);

        assertEquals(1, appointment.getDoctorId());
        assertEquals(1, appointment.getPatientId());
        assertEquals(MONDAY_9AM, appointment.getAppointmentDate());
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
        assertEquals("Checkup", appointment.getReason());
        assertEquals("Alice Adams", appointment.getDoctorName());
        assertEquals("Cardiology", appointment.getDoctorSpecialty());
        assertEquals("Carol Clark", appointment.getPatientName());
        assertNotNull(appointment.getCreatedAt());
    }

    @Test
    public void getAppointmentById_missing_returns_null() {
        assertNull(sut.getAppointmentById(999));
    }

    @Test
    public void getAppointments_without_filters_returns_all_in_date_order() {
        List<Appointment> all = sut.getAppointments(null, null, null, null);

        assertEquals(List.of(1, 2, 3, 4), all.stream().map(Appointment::getAppointmentId).toList());
    }

    @Test
    public void getAppointments_filters_by_doctor_and_patient() {
        assertEquals(3, sut.getAppointments(1, null, null, null).size());
        assertEquals(3, sut.getAppointments(null, 1, null, null).size());
        assertEquals(2, sut.getAppointments(1, 1, null, null).size());
    }

    @Test
    public void getAppointments_filters_by_whole_day() {
        List<Appointment> monday = sut.getAppointments(null, null, MONDAY, null);

        assertEquals(List.of(1, 2), monday.stream().map(Appointment::getAppointmentId).toList());
    }

    @Test
    public void getAppointments_filters_by_status() {
        List<Appointment> cancelled = sut.getAppointments(null, null, null, AppointmentStatus.CANCELLED);

        assertEquals(1, cancelled.size());
        assertEquals(3, cancelled.get(0).getAppointmentId());
    }

    @Test
    public void createAppointment_defaults_to_scheduled() {
        Appointment created = sut.createAppointment(
                new AppointmentRequestDto(2, 2, LocalDateTime.of(2030, 2, 1, 11, 30), "Rash"));

        assertTrue(created.getAppointmentId() > 0);
        assertEquals(AppointmentStatus.SCHEDULED, created.getStatus());
        assertEquals("Bob Brown", created.getDoctorName());
        assertEquals("Dan Davis", created.getPatientName());
    }

    @Test
    public void createAppointment_in_taken_slot_violates_unique_index() {
        assertThrows(DaoException.class,
                () -> sut.createAppointment(new AppointmentRequestDto(1, 2, MONDAY_9AM, "Double booking")));
    }

    @Test
    public void createAppointment_in_cancelled_slot_is_allowed() {
        LocalDateTime cancelledSlot = LocalDateTime.of(2030, 1, 8, 14, 0);
        assertNotNull(sut.createAppointment(new AppointmentRequestDto(2, 2, cancelledSlot, null)));
    }

    @Test
    public void updateAppointment_changes_time_doctor_and_reason() {
        LocalDateTime newTime = LocalDateTime.of(2030, 1, 14, 11, 0);
        Appointment updated = sut.updateAppointment(1, new AppointmentUpdateDto(2, newTime, "Moved"));

        assertEquals(2, updated.getDoctorId());
        assertEquals(newTime, updated.getAppointmentDate());
        assertEquals("Moved", updated.getReason());
        assertEquals(1, updated.getPatientId(), "patient is unchanged");
    }

    @Test
    public void updateAppointment_missing_returns_null() {
        assertNull(sut.updateAppointment(999, new AppointmentUpdateDto(1, MONDAY_9AM, null)));
    }

    @Test
    public void updateStatus_changes_status() {
        assertEquals(AppointmentStatus.CANCELLED, sut.updateStatus(1, AppointmentStatus.CANCELLED).getStatus());
        assertNull(sut.updateStatus(999, AppointmentStatus.CANCELLED));
    }

    @Test
    public void deleteAppointment_removes_row() {
        assertTrue(sut.deleteAppointment(1));
        assertNull(sut.getAppointmentById(1));
        assertFalse(sut.deleteAppointment(1));
    }

    @Test
    public void isDoctorAvailable_checks_active_appointments_only() {
        assertFalse(sut.isDoctorAvailable(1, MONDAY_9AM, null), "slot is booked");
        assertTrue(sut.isDoctorAvailable(1, MONDAY_9AM, 1), "the appointment being rescheduled is ignored");
        assertTrue(sut.isDoctorAvailable(2, LocalDateTime.of(2030, 1, 8, 14, 0), null), "cancelled slot is free");
        assertTrue(sut.isDoctorAvailable(1, MONDAY_9AM.plusMinutes(30), null));
    }

    @Test
    public void getBookedTimes_excludes_cancelled_and_other_days() {
        assertEquals(List.of(MONDAY_9AM, MONDAY_9AM.plusHours(1)), sut.getBookedTimes(1, MONDAY));
        assertTrue(sut.getBookedTimes(2, LocalDate.of(2030, 1, 8)).isEmpty());
    }
}
