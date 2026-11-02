package com.techelevator.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Returned from /login (with a token) and /me (without one). {@code doctorId} / {@code patientId}
 * identify the profile linked to the account so the frontend can call profile-specific endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDto {

    private String token;
    private User user;
    private Integer doctorId;
    private Integer patientId;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String token, User user, Integer doctorId, Integer patientId) {
        this.token = token;
        this.user = user;
        this.doctorId = doctorId;
        this.patientId = patientId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }
}
