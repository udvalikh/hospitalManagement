package com.techelevator.controller;

import com.techelevator.model.Doctor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DoctorControllerTest extends ControllerTestBase {

    private static final String PROFILE =
            "{\"firstName\":\"Ann\",\"lastName\":\"Lee\",\"specialty\":\"Dermatology\"," +
            "\"availableDays\":[\"MONDAY\"],\"availableTime\":\"09:00-12:00\"}";

    @Test
    void list_is_public_and_supports_specialty_filter() throws Exception {
        when(doctorDao.getDoctors("cardio")).thenReturn(List.of(doctor(1)));

        mvc.perform(get("/doctors").param("specialty", "cardio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(1));
    }

    @Test
    void specialties_is_public() throws Exception {
        when(doctorDao.getSpecialties()).thenReturn(List.of("Cardiology"));
        mvc.perform(get("/doctors/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Cardiology"));
    }

    @Test
    void get_missing_doctor_is_404() throws Exception {
        mvc.perform(get("/doctors/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Doctor 99 not found"));
    }

    @Test
    void get_with_non_numeric_id_is_400() throws Exception {
        mvc.perform(get("/doctors/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void slots_are_public() throws Exception {
        LocalDate day = LocalDate.of(2030, 1, 7);
        when(appointmentService.availableSlots(any(Doctor.class), eq(day))).thenReturn(List.of(day.atTime(9, 30)));

        mvc.perform(get("/doctors/1/slots").param("date", "2030-01-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2030-01-07T09:30:00"));
    }

    @Test
    void slots_without_date_is_400() throws Exception {
        mvc.perform(get("/doctors/1/slots")).andExpect(status().isBadRequest());
    }

    @Test
    void availability_reports_boolean() throws Exception {
        LocalDateTime when = LocalDateTime.of(2030, 1, 7, 9, 30);
        when(appointmentService.isSlotAvailable(any(Doctor.class), eq(when))).thenReturn(true);

        mvc.perform(get("/doctors/1/availability").param("dateTime", "2030-01-07T09:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void appointments_of_doctor_hidden_from_patients() throws Exception {
        mvc.perform(get("/doctors/1/appointments")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void doctor_can_see_own_appointments() throws Exception {
        mvc.perform(get("/doctors/1/appointments")).andExpect(status().isOk());
        verify(appointmentDao).getAppointments(1, null, null, null);
    }

    @Test
    @WithMockUser(username = "doc2", roles = "DOCTOR")
    void doctor_cannot_see_other_doctors_appointments() throws Exception {
        mvc.perform(get("/doctors/1/appointments")).andExpect(status().isForbidden());
    }

    @Test
    void create_requires_authentication() throws Exception {
        mvc.perform(post("/doctors").contentType(MediaType.APPLICATION_JSON).content(PROFILE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void create_as_patient_is_403() throws Exception {
        mvc.perform(post("/doctors").contentType(MediaType.APPLICATION_JSON).content(PROFILE))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void create_as_admin_is_201() throws Exception {
        when(accountService.createDoctor(any())).thenReturn(doctor(7));

        mvc.perform(post("/doctors").contentType(MediaType.APPLICATION_JSON)
                        .content(PROFILE.replace("{", "{\"username\":\"drann\",\"password\":\"password1\",")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(7));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void create_with_invalid_schedule_is_400() throws Exception {
        mvc.perform(post("/doctors").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"drann\",\"password\":\"password1\",\"firstName\":\"Ann\",\"lastName\":\"Lee\"," +
                                "\"availableDays\":[\"FUNDAY\"],\"availableTime\":\"9am-5pm\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.availableTime").exists())
                .andExpect(jsonPath("$.fieldErrors['availableDays[0]']").exists());
        verify(accountService, never()).createDoctor(any());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void doctor_can_update_own_profile() throws Exception {
        when(doctorDao.updateDoctor(eq(1), any())).thenReturn(doctor(1));
        mvc.perform(put("/doctors/1").contentType(MediaType.APPLICATION_JSON).content(PROFILE))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "doc2", roles = "DOCTOR")
    void doctor_cannot_update_other_profile() throws Exception {
        mvc.perform(put("/doctors/1").contentType(MediaType.APPLICATION_JSON).content(PROFILE))
                .andExpect(status().isForbidden());
        verify(doctorDao, never()).updateDoctor(anyInt(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void update_missing_doctor_is_404() throws Exception {
        mvc.perform(put("/doctors/99").contentType(MediaType.APPLICATION_JSON).content(PROFILE))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void delete_as_admin_is_204() throws Exception {
        when(doctorDao.deleteDoctor(1)).thenReturn(true);
        mvc.perform(delete("/doctors/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void delete_as_doctor_is_403() throws Exception {
        mvc.perform(delete("/doctors/1")).andExpect(status().isForbidden());
    }
}
