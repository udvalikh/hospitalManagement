package com.techelevator.service;

import com.techelevator.model.Doctor;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DoctorScheduleTest {

    private static final LocalDate MONDAY = LocalDate.of(2030, 1, 7);
    private static final LocalDate TUESDAY = MONDAY.plusDays(1);

    private static Doctor doctor(List<String> days, String hours) {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(1);
        doctor.setAvailableDays(days);
        doctor.setAvailableTime(hours);
        return doctor;
    }

    @Test
    void worksOn_empty_days_means_every_day() {
        Doctor doctor = doctor(List.of(), null);
        for (DayOfWeek day : DayOfWeek.values()) {
            assertTrue(DoctorSchedule.worksOn(doctor, day));
        }
    }

    @Test
    void worksOn_is_case_insensitive() {
        Doctor doctor = doctor(List.of("monday", " Wednesday "), null);
        assertTrue(DoctorSchedule.worksOn(doctor, DayOfWeek.MONDAY));
        assertTrue(DoctorSchedule.worksOn(doctor, DayOfWeek.WEDNESDAY));
        assertFalse(DoctorSchedule.worksOn(doctor, DayOfWeek.TUESDAY));
    }

    @Test
    void workingHours_parses_multiple_ranges() {
        List<DoctorSchedule.TimeRange> ranges = DoctorSchedule.workingHours(doctor(null, "09:00-12:00, 13:00-17:00"));

        assertEquals(2, ranges.size());
        assertEquals(LocalTime.of(13, 0), ranges.get(1).start());
        assertEquals(LocalTime.of(17, 0), ranges.get(1).end());
    }

    @Test
    void workingHours_defaults_when_missing_or_malformed() {
        DoctorSchedule.TimeRange fallback = new DoctorSchedule.TimeRange(DoctorSchedule.DEFAULT_START, DoctorSchedule.DEFAULT_END);
        assertEquals(List.of(fallback), DoctorSchedule.workingHours(doctor(null, null)));
        assertEquals(List.of(fallback), DoctorSchedule.workingHours(doctor(null, "garbage")));
        assertEquals(List.of(fallback), DoctorSchedule.workingHours(doctor(null, "17:00-09:00")));
    }

    @Test
    void isWithinSchedule_respects_days_and_range_end() {
        Doctor doctor = doctor(List.of("MONDAY"), "09:00-12:00");

        assertTrue(DoctorSchedule.isWithinSchedule(doctor, MONDAY.atTime(9, 0), 30));
        assertTrue(DoctorSchedule.isWithinSchedule(doctor, MONDAY.atTime(11, 30), 30), "last slot ends exactly at close");
        assertFalse(DoctorSchedule.isWithinSchedule(doctor, MONDAY.atTime(12, 0), 30));
        assertFalse(DoctorSchedule.isWithinSchedule(doctor, MONDAY.atTime(8, 30), 30));
        assertFalse(DoctorSchedule.isWithinSchedule(doctor, TUESDAY.atTime(9, 0), 30), "not a working day");
    }

    @Test
    void isWithinSchedule_rejects_slots_running_past_midnight() {
        Doctor doctor = doctor(null, "20:00-23:59");
        assertFalse(DoctorSchedule.isWithinSchedule(doctor, MONDAY.atTime(23, 45), 30));
    }

    @Test
    void slotStarts_lists_every_slot_in_every_range() {
        List<LocalDateTime> slots = DoctorSchedule.slotStarts(doctor(List.of("MONDAY"), "09:00-10:00,13:00-14:00"), MONDAY, 30);

        assertEquals(List.of(MONDAY.atTime(9, 0), MONDAY.atTime(9, 30), MONDAY.atTime(13, 0), MONDAY.atTime(13, 30)), slots);
    }

    @Test
    void slotStarts_empty_on_day_off() {
        assertTrue(DoctorSchedule.slotStarts(doctor(List.of("MONDAY"), null), TUESDAY, 30).isEmpty());
    }

    @Test
    void slotStarts_terminates_for_range_ending_near_midnight() {
        List<LocalDateTime> slots = DoctorSchedule.slotStarts(doctor(null, "23:00-23:59"), MONDAY, 30);
        assertEquals(List.of(MONDAY.atTime(23, 0)), slots);
    }
}
