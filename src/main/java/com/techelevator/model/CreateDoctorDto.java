package com.techelevator.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Admin-only body that creates a doctor's login account and profile together. */
public class CreateDoctorDto extends DoctorDto {
    @NotBlank(message = "username is required")
    @Size(max = 50)
    private String username;
    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be at least 8 characters")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
