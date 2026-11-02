package com.techelevator.controller;

import com.techelevator.exception.DaoException;
import com.techelevator.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PatientAndUserControllerTest extends ControllerTestBase {

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void patient_list_hidden_from_patients() throws Exception {
        mvc.perform(get("/patients")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void doctors_can_list_patients() throws Exception {
        mvc.perform(get("/patients")).andExpect(status().isOk());
        verify(patientDao).getPatients();
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void patient_can_view_own_profile() throws Exception {
        mvc.perform(get("/patients/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(5))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "pat2", roles = "PATIENT")
    void patient_cannot_view_other_profile() throws Exception {
        mvc.perform(get("/patients/5")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void update_with_invalid_email_is_400() throws Exception {
        mvc.perform(put("/patients/5").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void update_with_duplicate_email_is_409() throws Exception {
        when(patientDao.updatePatient(eq(5), any())).thenThrow(new DaoException("Data integrity violation",
                new DataIntegrityViolationException("duplicate key value violates unique constraint \"patients_email_key\"")));

        mvc.perform(put("/patients/5").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"carol@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("That email address is already in use"));
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void doctor_cannot_edit_patient_profile() throws Exception {
        mvc.perform(put("/patients/5").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void doctor_sees_only_own_appointments_with_patient() throws Exception {
        mvc.perform(get("/patients/5/appointments")).andExpect(status().isOk());
        verify(appointmentDao).getAppointments(1, 5, null, null);
    }

    @Test
    @WithMockUser(username = "pat", roles = "PATIENT")
    void patient_sees_all_own_appointments() throws Exception {
        mvc.perform(get("/patients/5/appointments")).andExpect(status().isOk());
        verify(appointmentDao).getAppointments(null, 5, null, null);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void delete_missing_patient_is_404() throws Exception {
        mvc.perform(delete("/patients/99")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_can_list_users_without_passwords() throws Exception {
        when(userDao.getUsers()).thenReturn(List.of(new User(1, "admin", "secret-hash", "ROLE_ADMIN")));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "doc", roles = "DOCTOR")
    void users_hidden_from_non_admins() throws Exception {
        mvc.perform(get("/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void unknown_route_is_404_json() throws Exception {
        mvc.perform(get("/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
