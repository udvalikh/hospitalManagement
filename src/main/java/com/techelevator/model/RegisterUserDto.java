package com.techelevator.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Public self-registration body. Self-registration always creates a PATIENT account:
 * {@code role} is ignored by the /register endpoint (it is used internally when an
 * admin creates other kinds of accounts).
 */
public class RegisterUserDto {

    @NotBlank(message = "username is required")
    @Size(max = 50)
    private String username;
    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be at least 8 characters")
    private String password;
    @NotBlank(message = "confirmPassword is required")
    private String confirmPassword;
    private String role;

    // Optional patient profile fields captured at sign-up.
    @Size(max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;
    @Email(message = "email must be a valid email address")
    @Size(max = 100)
    private String email;
    @Size(max = 20)
    private String phone;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
