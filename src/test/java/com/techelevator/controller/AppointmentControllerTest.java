package com.techelevator.controller;

import com.techelevator.exception.ConflictException;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerTest extends ControllerTestBase {

    private static final LocalDateTime WHEN = LocalDateTime.of(2030, 1, 7, 9, 0);
    private static final String BOOKING = "{\"doctorId\":1,\"patientId\":6,\"appointmentDate\":\"2030-01-07T09:00:00\",\"reason\":\"Checkup\"}";

    /** Appointment 10: doctor 1 with patient 5. */
    private Appointment givenAppointment(AppointmentStatus status) {
        Appointment appointment = new Appointment(10, 1, 5, WHEN, status, "Checkup");
        when(appointmentDao.getAppointmentById(10)).thenReturn(appointment);
        return appointment;
    }

    private static String statusBody(AppointmentStatus status) {
        return "{\"status\":\"" + status + "\"}";
    }

    @Test
    void list_requires_authentication() throws Exception {
        mvc.perform(get("/appointments")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void list_as_patient_is_forced_to_own_appointments() throws Exception {
        when(appointmentDao.getAppointments(null, 5, null, null)).thenReturn(List.of(new Appointment()));

        mvc.perform(get("/appointments").param("patientId", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        verify(appointmentDao).getAppointments(null, 5, null, null);
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void list_as_doctor_is_forced_to_own_schedule_and_supports_filters() throws Exception {
        mvc.perform(get("/appointments").param("doctorId", "2").param("date", "2030-01-07").param("status", "SCHEDULED"))
                .andExpect(status().isOk());
        verify(appointmentDao).getAppointments(1, null, LocalDate.of(2030, 1, 7), AppointmentStatus.SCHEDULED);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void list_as_admin_passes_filters_through() throws Exception {
        mvc.perform(get("/appointments").param("doctorId", "2").param("patientId", "6"))
                .andExpect(status().isOk());
        verify(appointmentDao).getAppointments(2, 6, null, null);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void list_with_bad_status_is_400() throws Exception {
        mvc.perform(get("/appointments").param("status", "BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'BOGUS' for parameter 'status'"));
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void get_own_appointment_includes_status_and_iso_date() throws Exception {
        givenAppointment(AppointmentStatus.SCHEDULED);

        mvc.perform(get("/appointments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.appointmentDate").value("2030-01-07T09:00:00"));
    }

    @Test
    @WithMockUser(username = "pat2", roles = "PATIENT")
    void get_someone_elses_appointment_is_403() throws Exception {
        givenAppointment(AppointmentStatus.SCHEDULED);
        mvc.perform(get("/appointments/10")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void get_missing_appointment_is_404() throws Exception {
        mvc.perform(get("/appointments/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Appointment 404 not found"));
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void create_as_patient_books_for_self_regardless_of_body() throws Exception {
        when(appointmentService.book(any())).thenReturn(new Appointment(11, 1, 5, WHEN, AppointmentStatus.SCHEDULED, null));

        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(BOOKING))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value(11));

        ArgumentCaptor<AppointmentRequestDto> captor = ArgumentCaptor.forClass(AppointmentRequestDto.class);
        verify(appointmentService).book(captor.capture());
        assertEquals(5, captor.getValue().getPatientId());
        assertEquals(WHEN, captor.getValue().getAppointmentDate());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void create_as_admin_uses_patient_from_body() throws Exception {
        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(BOOKING))
                .andExpect(status().isCreated());

        ArgumentCaptor<AppointmentRequestDto> captor = ArgumentCaptor.forClass(AppointmentRequestDto.class);
        verify(appointmentService).book(captor.capture());
        assertEquals(6, captor.getValue().getPatientId());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void create_as_doctor_is_403() throws Exception {
        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(BOOKING))
                .andExpect(status().isForbidden());
        verify(appointmentService, never()).book(any());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void create_with_missing_fields_is_400() throws Exception {
        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.doctorId").exists())
                .andExpect(jsonPath("$.fieldErrors.appointmentDate").exists());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void create_with_malformed_date_is_400() throws Exception {
        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorId\":1,\"appointmentDate\":\"next tuesday\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void create_in_taken_slot_is_409() throws Exception {
        when(appointmentService.book(any())).thenThrow(new ConflictException("The doctor already has an appointment at that time"));

        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(BOOKING))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The doctor already has an appointment at that time"));
    }

    @Test
    @WithMockUser(username = "pat2", roles = "PATIENT")
    void reschedule_someone_elses_appointment_is_403() throws Exception {
        givenAppointment(AppointmentStatus.SCHEDULED);
        mvc.perform(put("/appointments/10").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorId\":1,\"appointmentDate\":\"2030-01-07T09:30:00\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void reschedule_own_appointment_delegates_to_service() throws Exception {
        Appointment existing = givenAppointment(AppointmentStatus.SCHEDULED);
        mvc.perform(put("/appointments/10").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorId\":1,\"appointmentDate\":\"2030-01-07T09:30:00\"}"))
                .andExpect(status().isOk());
        verify(appointmentService).reschedule(eq(existing), any());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void patient_can_cancel_own_appointment() throws Exception {
        Appointment existing = givenAppointment(AppointmentStatus.SCHEDULED);
        mvc.perform(patch("/appointments/10/status").contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody(AppointmentStatus.CANCELLED)))
                .andExpect(status().isOk());
        verify(appointmentService).changeStatus(existing, AppointmentStatus.CANCELLED);
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void patient_cannot_mark_completed() throws Exception {
        givenAppointment(AppointmentStatus.SCHEDULED);
        mvc.perform(patch("/appointments/10/status").contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody(AppointmentStatus.COMPLETED)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void doctor_can_complete_own_appointment() throws Exception {
        Appointment existing = givenAppointment(AppointmentStatus.SCHEDULED);
        mvc.perform(patch("/appointments/10/status").contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody(AppointmentStatus.COMPLETED)))
                .andExpect(status().isOk());
        verify(appointmentService).changeStatus(existing, AppointmentStatus.COMPLETED);
    }

    @Test
    @WithMockUser(username = "doc2", roles = "DOCTOR")
    void other_doctor_cannot_change_status() throws Exception {
        givenAppointment(AppointmentStatus.SCHEDULED);
        mvc.perform(patch("/appointments/10/status").contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody(AppointmentStatus.CANCELLED)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void doctor_cannot_change_already_completed_appointment() throws Exception {
        givenAppointment(AppointmentStatus.COMPLETED);
        mvc.perform(patch("/appointments/10/status").contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody(AppointmentStatus.CANCELLED)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void delete_as_patient_is_403() throws Exception {
        mvc.perform(delete("/appointments/10")).andExpect(status().isForbidden());
        verify(appointmentDao, never()).deleteAppointment(10);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void delete_as_admin() throws Exception {
        when(appointmentDao.deleteAppointment(10)).thenReturn(true);
        mvc.perform(delete("/appointments/10")).andExpect(status().isNoContent());
        mvc.perform(delete("/appointments/11")).andExpect(status().isNotFound());
    }
}
