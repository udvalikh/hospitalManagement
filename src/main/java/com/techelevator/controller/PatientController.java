package com.techelevator.controller;

import com.techelevator.dao.AppointmentDao;
import com.techelevator.dao.PatientDao;
import com.techelevator.exception.NotFoundException;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentStatus;
import com.techelevator.model.Patient;
import com.techelevator.model.PatientDto;
import com.techelevator.security.CurrentUser;
import com.techelevator.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientDao patientDao;
    private final AppointmentDao appointmentDao;
    private final CurrentUserService currentUserService;

    public PatientController(PatientDao patientDao, AppointmentDao appointmentDao, CurrentUserService currentUserService) {
        this.patientDao = patientDao;
        this.appointmentDao = appointmentDao;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public List<Patient> list() {
        return patientDao.getPatients();
    }

    /** Admins, doctors, or the patient themselves. */
    @GetMapping("/{id}")
    public Patient get(@PathVariable int id, Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        if (!me.isAdmin() && !me.isDoctor() && !me.isPatient(id)) {
            throw new AccessDeniedException("Not your profile");
        }
        return requirePatient(id);
    }

    /** Admins, or the patient editing their own profile. */
    @PutMapping("/{id}")
    public Patient update(@PathVariable int id, @Valid @RequestBody PatientDto patient, Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        if (!me.isAdmin() && !me.isPatient(id)) {
            throw new AccessDeniedException("Not your profile");
        }
        Patient updated = patientDao.updatePatient(id, patient);
        if (updated == null) {
            throw new NotFoundException("Patient " + id + " not found");
        }
        return updated;
    }

    /** Admin only. Also removes the patient's login and appointments. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable int id) {
        if (!patientDao.deletePatient(id)) {
            throw new NotFoundException("Patient " + id + " not found");
        }
    }

    /** A patient's appointments. Doctors only see the ones booked with them. */
    @GetMapping("/{id}/appointments")
    public List<Appointment> appointments(@PathVariable int id,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                          @RequestParam(required = false) AppointmentStatus status,
                                          Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        Integer doctorFilter = null;
        if (!me.isAdmin() && !me.isPatient(id)) {
            if (!me.isDoctor()) {
                throw new AccessDeniedException("Not your appointments");
            }
            doctorFilter = AppointmentController.requireProfile(me.doctorId());
        }
        requirePatient(id);
        return appointmentDao.getAppointments(doctorFilter, id, date, status);
    }

    private Patient requirePatient(int id) {
        Patient patient = patientDao.getPatientById(id);
        if (patient == null) {
            throw new NotFoundException("Patient " + id + " not found");
        }
        return patient;
    }
}
