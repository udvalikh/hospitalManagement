package com.techelevator.controller;

import com.jayway.jsonpath.JsonPath;
import com.techelevator.exception.ConflictException;
import com.techelevator.model.Patient;
import com.techelevator.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationControllerTest extends ControllerTestBase {

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void login_returns_token_user_and_linked_profile() throws Exception {
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"pat\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, org.hamcrest.Matchers.startsWith("Bearer ")))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("pat"))
                .andExpect(jsonPath("$.user.role").value("ROLE_PATIENT"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.patientId").value(5))
                .andExpect(jsonPath("$.doctorId").doesNotExist());
    }

    @Test
    void login_as_doctor_returns_doctor_id() throws Exception {
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"doc\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(1));
    }

    @Test
    void login_wrong_password_is_401_json() throws Exception {
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"pat\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username or password is incorrect"));
    }

    @Test
    void login_unknown_user_is_401() throws Exception {
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missing_fields_is_400_with_field_errors() throws Exception {
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void token_from_login_authenticates_me() throws Exception {
        String token = login("doc", "password");

        mvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("doc"))
                .andExpect(jsonPath("$.doctorId").value(1))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void me_without_token_is_401_json() throws Exception {
        mvc.perform(get("/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/me"));
    }

    @Test
    void invalid_token_is_401() throws Exception {
        mvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_creates_patient_and_returns_token() throws Exception {
        User user = new User(9, "newbie", null, "ROLE_PATIENT");
        when(accountService.registerPatient(any())).thenReturn(new Patient(12, user, null, null, null, null, null));

        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newbie\",\"password\":\"password1\",\"confirmPassword\":\"password1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.patientId").value(12))
                .andExpect(jsonPath("$.user.role").value("ROLE_PATIENT"));
    }

    @Test
    void register_short_password_is_400() throws Exception {
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newbie\",\"password\":\"short\",\"confirmPassword\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void register_taken_username_is_409() throws Exception {
        when(accountService.registerPatient(any())).thenThrow(new ConflictException("Username 'pat' is already taken"));

        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"pat\",\"password\":\"password1\",\"confirmPassword\":\"password1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username 'pat' is already taken"));
    }

    @Test
    void cors_preflight_allows_configured_frontend_origin() throws Exception {
        mvc.perform(options("/appointments")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    void cors_rejects_unknown_origin() throws Exception {
        mvc.perform(options("/appointments")
                        .header(HttpHeaders.ORIGIN, "http://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
