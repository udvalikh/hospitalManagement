package com.techelevator.service;

import com.techelevator.dao.AppointmentDao;
import com.techelevator.dao.DoctorDao;
import com.techelevator.dao.PatientDao;
import com.techelevator.exception.BadRequestException;
import com.techelevator.exception.ConflictException;
import com.techelevator.exception.NotFoundException;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentStatus;
import com.techelevator.model.AppointmentUpdateDto;
import com.techelevator.model.Doctor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Booking rules shared by the appointment and doctor endpoints. */
@Service
public class AppointmentService {

    /** Every appointment is one fixed-length slot. Start times must fall on a slot boundary. */
    public static final int SLOT_MINUTES = 30;

    private final AppointmentDao appointmentDao;
    private final DoctorDao doctorDao;
    private final PatientDao patientDao;
    private final Clock clock;

    public AppointmentService(AppointmentDao appointmentDao, DoctorDao doctorDao, PatientDao patientDao, Clock clock) {
        this.appointmentDao = appointmentDao;
        this.doctorDao = doctorDao;
        this.patientDao = patientDao;
        this.clock = clock;
    }

    public Appointment book(AppointmentRequestDto request) {
        if (request.getPatientId() == null) {
            throw new BadRequestException("patientId is required");
        }
        if (patientDao.getPatientById(request.getPatientId()) == null) {
            throw new BadRequestException("Patient " + request.getPatientId() + " does not exist");
        }
        Doctor doctor = requireDoctor(request.getDoctorId());
        validateSlot(doctor, request.getAppointmentDate(), null);
        return appointmentDao.createAppointment(request);
    }

    public Appointment reschedule(Appointment existing, AppointmentUpdateDto update) {
        if (existing.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled appointments can be changed");
        }
        Doctor doctor = requireDoctor(update.getDoctorId());
        boolean sameSlot = doctor.getDoctorId() == existing.getDoctorId()
                && update.getAppointmentDate().equals(existing.getAppointmentDate());
        if (!sameSlot) {
            validateSlot(doctor, update.getAppointmentDate(), existing.getAppointmentId());
        }
        Appointment updated = appointmentDao.updateAppointment(existing.getAppointmentId(), update);
        if (updated == null) {
            throw new NotFoundException("Appointment " + existing.getAppointmentId() + " not found");
        }
        return updated;
    }

    public Appointment changeStatus(Appointment existing, AppointmentStatus status) {
        if (status == AppointmentStatus.SCHEDULED && existing.getStatus() != AppointmentStatus.SCHEDULED) {
            // Re-opening must not double-book a slot someone else has taken since.
            if (!appointmentDao.isDoctorAvailable(existing.getDoctorId(), existing.getAppointmentDate(),
                    existing.getAppointmentId())) {
                throw new ConflictException("That time slot has been booked by someone else");
            }
        }
        Appointment updated = appointmentDao.updateStatus(existing.getAppointmentId(), status);
        if (updated == null) {
            throw new NotFoundException("Appointment " + existing.getAppointmentId() + " not found");
        }
        return updated;
    }

    /** Free, future slot start times for the doctor on the given day. */
    public List<LocalDateTime> availableSlots(Doctor doctor, LocalDate date) {
        LocalDateTime now = LocalDateTime.now(clock);
        Set<LocalDateTime> booked = new HashSet<>(appointmentDao.getBookedTimes(doctor.getDoctorId(), date));
        return DoctorSchedule.slotStarts(doctor, date, SLOT_MINUTES).stream()
                .filter(slot -> slot.isAfter(now))
                .filter(slot -> !booked.contains(slot))
                .toList();
    }

    /** True if a booking at {@code dateTime} would be accepted right now. */
    public boolean isSlotAvailable(Doctor doctor, LocalDateTime dateTime) {
        try {
            validateSlot(doctor, dateTime, null);
            return true;
        } catch (BadRequestException | ConflictException e) {
            return false;
        }
    }

    private Doctor requireDoctor(Integer doctorId) {
        Doctor doctor = doctorId == null ? null : doctorDao.getDoctorById(doctorId);
        if (doctor == null) {
            throw new BadRequestException("Doctor " + doctorId + " does not exist");
        }
        return doctor;
    }

    private void validateSlot(Doctor doctor, LocalDateTime start, Integer excludeAppointmentId) {
        if (start == null) {
            throw new BadRequestException("appointmentDate is required");
        }
        if (!start.isAfter(LocalDateTime.now(clock))) {
            throw new BadRequestException("Appointments must be booked in the future");
        }
        if (start.getSecond() != 0 || start.getNano() != 0 || start.getMinute() % SLOT_MINUTES != 0) {
            throw new BadRequestException("Appointments must start on a " + SLOT_MINUTES + "-minute boundary (e.g. 09:00 or 09:30)");
        }
        if (!DoctorSchedule.isWithinSchedule(doctor, start, SLOT_MINUTES)) {
            throw new BadRequestException("That time is outside the doctor's working hours");
        }
        if (!appointmentDao.isDoctorAvailable(doctor.getDoctorId(), start, excludeAppointmentId)) {
            throw new ConflictException("The doctor already has an appointment at that time");
        }
    }
}
