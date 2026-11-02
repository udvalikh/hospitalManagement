package com.techelevator.service;

import com.techelevator.model.Doctor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interprets a doctor's {@code availableDays} ("MONDAY,WEDNESDAY") and {@code availableTime}
 * ("09:00-12:00,13:00-17:00") settings. Missing days mean "every day"; missing hours mean 09:00-17:00.
 */
public final class DoctorSchedule {

    public static final LocalTime DEFAULT_START = LocalTime.of(9, 0);
    public static final LocalTime DEFAULT_END = LocalTime.of(17, 0);

    private DoctorSchedule() {
    }

    public record TimeRange(LocalTime start, LocalTime end) {
    }

    public static boolean worksOn(Doctor doctor, DayOfWeek day) {
        List<String> days = doctor.getAvailableDays();
        if (days == null || days.isEmpty()) {
            return true;
        }
        return days.stream().anyMatch(d -> d != null && d.trim().equalsIgnoreCase(day.name()));
    }

    public static List<TimeRange> workingHours(Doctor doctor) {
        List<TimeRange> ranges = new ArrayList<>();
        String spec = doctor.getAvailableTime();
        if (spec != null) {
            for (String part : spec.split(",")) {
                String[] bounds = part.trim().split("-");
                if (bounds.length != 2) {
                    continue;
                }
                try {
                    LocalTime start = LocalTime.parse(bounds[0].trim());
                    LocalTime end = LocalTime.parse(bounds[1].trim());
                    if (start.isBefore(end)) {
                        ranges.add(new TimeRange(start, end));
                    }
                } catch (DateTimeParseException ignored) {
                    // Skip malformed segments rather than failing the whole schedule.
                }
            }
        }
        if (ranges.isEmpty()) {
            ranges.add(new TimeRange(DEFAULT_START, DEFAULT_END));
        }
        return ranges;
    }

    /** True if an appointment of {@code durationMinutes} starting at {@code start} fits in the doctor's hours. */
    public static boolean isWithinSchedule(Doctor doctor, LocalDateTime start, int durationMinutes) {
        if (!worksOn(doctor, start.getDayOfWeek())) {
            return false;
        }
        LocalTime begin = start.toLocalTime();
        LocalTime finish = begin.plusMinutes(durationMinutes);
        if (!finish.isAfter(begin)) {
            return false; // would run past midnight
        }
        return workingHours(doctor).stream()
                .anyMatch(r -> !begin.isBefore(r.start()) && !finish.isAfter(r.end()));
    }

    /** Every slot start time on {@code date}, ignoring existing bookings. */
    public static List<LocalDateTime> slotStarts(Doctor doctor, LocalDate date, int durationMinutes) {
        List<LocalDateTime> slots = new ArrayList<>();
        if (!worksOn(doctor, date.getDayOfWeek())) {
            return slots;
        }
        for (TimeRange range : workingHours(doctor)) {
            LocalTime t = range.start();
            while (true) {
                LocalTime next = t.plusMinutes(durationMinutes);
                if (!next.isAfter(t) || next.isAfter(range.end())) {
                    break; // wrapped past midnight or past the end of the range
                }
                slots.add(date.atTime(t));
                t = next;
            }
        }
        slots.sort(null);
        return slots;
    }
}
