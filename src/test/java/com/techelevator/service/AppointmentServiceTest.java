package com.techelevator.service;

import com.techelevator.dao.AppointmentDao;
import com.techelevator.dao.DoctorDao;
import com.techelevator.dao.PatientDao;
import com.techelevator.exception.BadRequestException;
import com.techelevator.exception.ConflictException;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentStatus;
import com.techelevator.model.AppointmentUpdateDto;
import com.techelevator.model.Doctor;
import com.techelevator.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    // "Now" is Monday 2030-01-07 at 10:15.
    private static final ZoneId ZONE = ZoneId.of("UTC");
    private static final LocalDate MONDAY = LocalDate.of(2030, 1, 7);
    private static final Clock CLOCK = Clock.fixed(MONDAY.atTime(10, 15).atZone(ZONE).toInstant(), ZONE);
    private static final LocalDateTime NEXT_MONDAY_9 = MONDAY.plusWeeks(1).atTime(9, 0);

    private AppointmentDao appointmentDao;
    private DoctorDao doctorDao;
    private PatientDao patientDao;
    private AppointmentService sut;
    private Doctor doctor;

    @BeforeEach
    void setup() {
        appointmentDao = mock(AppointmentDao.class);
        doctorDao = mock(DoctorDao.class);
        patientDao = mock(PatientDao.class);
        sut = new AppointmentService(appointmentDao, doctorDao, patientDao, CLOCK);

        doctor = new Doctor();
        doctor.setDoctorId(1);
        doctor.setAvailableDays(List.of("MONDAY"));
        doctor.setAvailableTime("09:00-12:00");
        when(doctorDao.getDoctorById(1)).thenReturn(doctor);
        when(patientDao.getPatientById(5)).thenReturn(new Patient());
        when(appointmentDao.isDoctorAvailable(anyInt(), any(), any())).thenReturn(true);
    }

    @Test
    void book_valid_request_creates_appointment() {
        AppointmentRequestDto request = new AppointmentRequestDto(1, 5, NEXT_MONDAY_9, "Checkup");
        Appointment created = new Appointment();
        when(appointmentDao.createAppointment(request)).thenReturn(created);

        assertSame(created, sut.book(request));
    }

    @Test
    void book_in_the_past_is_rejected() {
        AppointmentRequestDto request = new AppointmentRequestDto(1, 5, MONDAY.atTime(9, 0), null);
        assertThrows(BadRequestException.class, () -> sut.book(request));
        verify(appointmentDao, never()).createAppointment(any());
    }

    @Test
    void book_off_slot_boundary_is_rejected() {
        assertThrows(BadRequestException.class,
                () -> sut.book(new AppointmentRequestDto(1, 5, NEXT_MONDAY_9.plusMinutes(15), null)));
    }

    @Test
    void book_outside_working_hours_is_rejected() {
        assertThrows(BadRequestException.class,
                () -> sut.book(new AppointmentRequestDto(1, 5, NEXT_MONDAY_9.plusHours(4), null)));
        assertThrows(BadRequestException.class,
                () -> sut.book(new AppointmentRequestDto(1, 5, NEXT_MONDAY_9.plusDays(1), null)), "Tuesday");
    }

    @Test
    void book_taken_slot_is_conflict() {
        when(appointmentDao.isDoctorAvailable(1, NEXT_MONDAY_9, null)).thenReturn(false);
        assertThrows(ConflictException.class, () -> sut.book(new AppointmentRequestDto(1, 5, NEXT_MONDAY_9, null)));
    }

    @Test
    void book_unknown_doctor_or_patient_is_rejected() {
        assertThrows(BadRequestException.class, () -> sut.book(new AppointmentRequestDto(99, 5, NEXT_MONDAY_9, null)));
        assertThrows(BadRequestException.class, () -> sut.book(new AppointmentRequestDto(1, 99, NEXT_MONDAY_9, null)));
        assertThrows(BadRequestException.class, () -> sut.book(new AppointmentRequestDto(1, null, NEXT_MONDAY_9, null)));
    }

    @Test
    void reschedule_excludes_itself_from_conflict_check() {
        Appointment existing = new Appointment(7, 1, 5, NEXT_MONDAY_9, AppointmentStatus.SCHEDULED, null);
        AppointmentUpdateDto update = new AppointmentUpdateDto(1, NEXT_MONDAY_9.plusMinutes(30), "Later");
        when(appointmentDao.updateAppointment(7, update)).thenReturn(existing);

        sut.reschedule(existing, update);

        verify(appointmentDao).isDoctorAvailable(1, NEXT_MONDAY_9.plusMinutes(30), 7);
    }

    @Test
    void reschedule_reason_only_skips_slot_validation() {
        Appointment existing = new Appointment(7, 1, 5, NEXT_MONDAY_9, AppointmentStatus.SCHEDULED, null);
        AppointmentUpdateDto update = new AppointmentUpdateDto(1, NEXT_MONDAY_9, "New reason");
        when(appointmentDao.updateAppointment(7, update)).thenReturn(existing);

        sut.reschedule(existing, update);

        verify(appointmentDao, never()).isDoctorAvailable(anyInt(), any(), any());
    }

    @Test
    void reschedule_cancelled_appointment_is_rejected() {
        Appointment existing = new Appointment(7, 1, 5, NEXT_MONDAY_9, AppointmentStatus.CANCELLED, null);
        assertThrows(BadRequestException.class,
                () -> sut.reschedule(existing, new AppointmentUpdateDto(1, NEXT_MONDAY_9, null)));
    }

    @Test
    void changeStatus_reopening_checks_slot_is_still_free() {
        Appointment existing = new Appointment(7, 1, 5, NEXT_MONDAY_9, AppointmentStatus.CANCELLED, null);
        when(appointmentDao.isDoctorAvailable(1, NEXT_MONDAY_9, 7)).thenReturn(false);

        assertThrows(ConflictException.class, () -> sut.changeStatus(existing, AppointmentStatus.SCHEDULED));
    }

    @Test
    void changeStatus_cancel_updates_status() {
        Appointment existing = new Appointment(7, 1, 5, NEXT_MONDAY_9, AppointmentStatus.SCHEDULED, null);
        Appointment cancelled = new Appointment(7, 1, 5, NEXT_MONDAY_9, AppointmentStatus.CANCELLED, null);
        when(appointmentDao.updateStatus(7, AppointmentStatus.CANCELLED)).thenReturn(cancelled);

        assertEquals(AppointmentStatus.CANCELLED, sut.changeStatus(existing, AppointmentStatus.CANCELLED).getStatus());
    }

    @Test
    void availableSlots_excludes_past_and_booked_slots() {
        when(appointmentDao.getBookedTimes(1, MONDAY)).thenReturn(List.of(MONDAY.atTime(11, 0)));

        List<LocalDateTime> slots = sut.availableSlots(doctor, MONDAY);

        // Now is 10:15, so 09:00-10:00 are gone and 11:00 is booked.
        assertEquals(List.of(MONDAY.atTime(10, 30), MONDAY.atTime(11, 30)), slots);
    }

    @Test
    void isSlotAvailable_reports_without_throwing() {
        assertTrue(sut.isSlotAvailable(doctor, NEXT_MONDAY_9));
        assertFalse(sut.isSlotAvailable(doctor, NEXT_MONDAY_9.plusDays(1)));
        when(appointmentDao.isDoctorAvailable(eq(1), eq(NEXT_MONDAY_9), any())).thenReturn(false);
        assertFalse(sut.isSlotAvailable(doctor, NEXT_MONDAY_9));
    }
}
