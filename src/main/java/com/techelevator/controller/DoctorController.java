package com.techelevator.controller;

import com.techelevator.dao.AppointmentDao;
import com.techelevator.dao.DoctorDao;
import com.techelevator.exception.NotFoundException;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentStatus;
import com.techelevator.model.CreateDoctorDto;
import com.techelevator.model.Doctor;
import com.techelevator.model.DoctorDto;
import com.techelevator.security.CurrentUser;
import com.techelevator.security.CurrentUserService;
import com.techelevator.service.AccountService;
import com.techelevator.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorDao doctorDao;
    private final AppointmentDao appointmentDao;
    private final AppointmentService appointmentService;
    private final AccountService accountService;
    private final CurrentUserService currentUserService;

    public DoctorController(DoctorDao doctorDao, AppointmentDao appointmentDao, AppointmentService appointmentService,
                            AccountService accountService, CurrentUserService currentUserService) {
        this.doctorDao = doctorDao;
        this.appointmentDao = appointmentDao;
        this.appointmentService = appointmentService;
        this.accountService = accountService;
        this.currentUserService = currentUserService;
    }

    /** Public. Optional {@code ?specialty=} does a case-insensitive partial match. */
    @GetMapping
    public List<Doctor> list(@RequestParam(required = false) String specialty) {
        return doctorDao.getDoctors(specialty);
    }

    /** Public. Distinct specialties, handy for a filter dropdown. */
    @GetMapping("/specialties")
    public List<String> specialties() {
        return doctorDao.getSpecialties();
    }

    /** Public. */
    @GetMapping("/{id}")
    public Doctor get(@PathVariable int id) {
        return requireDoctor(id);
    }

    /** Public. Free 30-minute slot start times on a day, e.g. {@code /doctors/1/slots?date=2030-01-07}. */
    @GetMapping("/{id}/slots")
    public List<LocalDateTime> slots(@PathVariable int id,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.availableSlots(requireDoctor(id), date);
    }

    /** Public. Whether a specific start time can be booked, e.g. {@code ?dateTime=2030-01-07T09:30:00}. */
    @GetMapping("/{id}/availability")
    public Map<String, Object> availability(@PathVariable int id,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        boolean available = appointmentService.isSlotAvailable(requireDoctor(id), dateTime);
        return Map.of("doctorId", id, "dateTime", dateTime, "available", available);
    }

    /** The doctor's own schedule (or any doctor's, for admins). */
    @GetMapping("/{id}/appointments")
    public List<Appointment> appointments(@PathVariable int id,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                          @RequestParam(required = false) AppointmentStatus status,
                                          Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        if (!me.isAdmin() && !me.isDoctor(id)) {
            throw new AccessDeniedException("Not your schedule");
        }
        requireDoctor(id);
        return appointmentDao.getAppointments(id, null, date, status);
    }

    /** Admin only: creates the doctor's login account and profile together. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Doctor create(@Valid @RequestBody CreateDoctorDto doctor) {
        return accountService.createDoctor(doctor);
    }

    /** Admins, or the doctor editing their own profile. */
    @PutMapping("/{id}")
    public Doctor update(@PathVariable int id, @Valid @RequestBody DoctorDto doctor, Principal principal) {
        CurrentUser me = currentUserService.get(principal);
        if (!me.isAdmin() && !me.isDoctor(id)) {
            throw new AccessDeniedException("Not your profile");
        }
        Doctor updated = doctorDao.updateDoctor(id, doctor);
        if (updated == null) {
            throw new NotFoundException("Doctor " + id + " not found");
        }
        return updated;
    }

    /** Admin only. Also removes the doctor's login and appointments. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable int id) {
        if (!doctorDao.deleteDoctor(id)) {
            throw new NotFoundException("Doctor " + id + " not found");
        }
    }

    private Doctor requireDoctor(int id) {
        Doctor doctor = doctorDao.getDoctorById(id);
        if (doctor == null) {
            throw new NotFoundException("Doctor " + id + " not found");
        }
        return doctor;
    }
}
