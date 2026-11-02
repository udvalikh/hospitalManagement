package com.techelevator.controller;

import com.techelevator.dao.AppointmentDao;
import com.techelevator.exception.BadRequestException;
import com.techelevator.exception.NotFoundException;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentStatus;
import com.techelevator.model.AppointmentStatusDto;
import com.techelevator.model.AppointmentUpdateDto;
import com.techelevator.security.CurrentUser;
import com.techelevator.security.CurrentUserService;
import com.techelevator.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/**
 * Appointments. Admins see everything; doctors see their own schedule; patients see and manage their own bookings.
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentDao appointmentDao;
    private final AppointmentService appointmentService;
    private final CurrentUserService currentUserService;

    public AppointmentController(AppointmentDao appointmentDao, AppointmentService appointmentService,
                                 CurrentUserService currentUserService) {
        this.appointmentDao = appointmentDao;
        this.appointmentService = appointmentService;
        this.currentUserService = currentUserService;
    }

    /**
     * Lists appointments visible to the caller. Optional filters: doctorId, patientId,
     * date (yyyy-MM-dd, whole day) and status.
     */
    @GetMapping
    public List<Appointment> list(@RequestParam(required = false) Integer doctorId,
                                  @RequestParam(required = false) Integer patientId,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam(required = false) AppointmentStatus status,
                                  Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        if (!me.isAdmin()) {
            if (me.isDoctor()) {
                doctorId = requireProfile(me.doctorId());
            } else if (me.isPatient()) {
                patientId = requireProfile(me.patientId());
            } else {
                throw new AccessDeniedException("No appointment access");
            }
        }
        return appointmentDao.getAppointments(doctorId, patientId, date, status);
    }

    @GetMapping("/{id}")
    public Appointment get(@PathVariable int id, Principal principal) {
        Appointment appointment = requireAppointment(id);
        requireParticipant(currentUserService.get(principal), appointment);
        return appointment;
    }

    /** Patients book for themselves; admins may book for any patient by passing patientId. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Appointment create(@Valid @RequestBody AppointmentRequestDto request, Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        if (!me.isAdmin()) {
            request.setPatientId(requireProfile(me.patientId()));
        }
        return appointmentService.book(request);
    }

    /** Reschedule (doctor, time, reason). Allowed for admins and the patient who booked it. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Appointment update(@PathVariable int id, @Valid @RequestBody AppointmentUpdateDto update, Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        Appointment existing = requireAppointment(id);
        if (!me.isAdmin() && !me.isPatient(existing.getPatientId())) {
            throw new AccessDeniedException("Not your appointment");
        }
        return appointmentService.reschedule(existing, update);
    }

    /**
     * Change status. Patients may cancel their own appointment; doctors may complete or cancel theirs;
     * admins may set any status.
     */
    @PatchMapping("/{id}/status")
    public Appointment updateStatus(@PathVariable int id, @Valid @RequestBody AppointmentStatusDto body,
                                    Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        Appointment existing = requireAppointment(id);
        AppointmentStatus target = body.getStatus();

        if (!me.isAdmin()) {
            boolean ownsAsDoctor = me.isDoctor(existing.getDoctorId());
            boolean ownsAsPatient = me.isPatient(existing.getPatientId());
            boolean allowed = (ownsAsDoctor && target != AppointmentStatus.SCHEDULED)
                    || (ownsAsPatient && target == AppointmentStatus.CANCELLED);
            if (!allowed) {
                throw new AccessDeniedException("Not allowed to set this status");
            }
            if (existing.getStatus() != AppointmentStatus.SCHEDULED) {
                throw new BadRequestException("Only scheduled appointments can be " + target.name().toLowerCase());
            }
        }
        return appointmentService.changeStatus(existing, target);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable int id) {
        if (!appointmentDao.deleteAppointment(id)) {
            throw new NotFoundException("Appointment " + id + " not found");
        }
    }

    private Appointment requireAppointment(int id) {
        Appointment appointment = appointmentDao.getAppointmentById(id);
        if (appointment == null) {
            throw new NotFoundException("Appointment " + id + " not found");
        }
        return appointment;
    }

    static void requireParticipant(CurrentUser me, Appointment appointment) {
        if (!me.isAdmin() && !me.isDoctor(appointment.getDoctorId()) && !me.isPatient(appointment.getPatientId())) {
            throw new AccessDeniedException("Not your appointment");
        }
    }

    static int requireProfile(Integer profileId) {
        if (profileId == null) {
            throw new AccessDeniedException("Your account has no linked profile");
        }
        return profileId;
    }
}
