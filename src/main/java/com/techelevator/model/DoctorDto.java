package com.techelevator.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/** Editable doctor profile fields. */
public class DoctorDto {
    private static final String HH_MM = "([01]\\d|2[0-3]):[0-5]\\d";
    private static final String RANGE = HH_MM + "-" + HH_MM;
    public static final String TIME_RANGES_REGEX = "^" + RANGE + "(," + RANGE + ")*$";

    @NotBlank(message = "firstName is required")
    @Size(max = 100)
    private String firstName;
    @NotBlank(message = "lastName is required")
    @Size(max = 100)
    private String lastName;
    @Size(max = 100)
    private String specialty;
    @Size(max = 20)
    private String phone;
    /** Day names, e.g. ["MONDAY","WEDNESDAY"]. Empty means every day. */
    private List<@Pattern(regexp = "(?i)MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY",
            message = "availableDays must contain day names such as MONDAY") String> availableDays = new ArrayList<>();
    /** Working hours, e.g. "09:00-12:00,13:00-17:00". Empty means 09:00-17:00. */
    @Pattern(regexp = TIME_RANGES_REGEX, message = "availableTime must look like 09:00-12:00,13:00-17:00")
    private String availableTime;

    public DoctorDto() {
    }

    public DoctorDto(String firstName, String lastName, String specialty, String phone,
                     List<String> availableDays, String availableTime) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialty = specialty;
        this.phone = phone;
        this.availableDays = availableDays;
        this.availableTime = availableTime;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }

    public String getAvailableTime() { return availableTime; }
    public void setAvailableTime(String availableTime) { this.availableTime = availableTime; }
}
