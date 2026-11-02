package com.techelevator.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Editable patient profile fields. */
public class PatientDto {
    @Size(max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;
    @Size(max = 50)
    private String insuranceNumber;
    @Size(max = 20)
    private String phone;
    @Email(message = "email must be a valid email address")
    @Size(max = 100)
    private String email;

    public PatientDto() {
    }

    public PatientDto(String firstName, String lastName, String insuranceNumber, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.insuranceNumber = insuranceNumber;
        this.phone = phone;
        this.email = email;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getInsuranceNumber() { return insuranceNumber; }
    public void setInsuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
