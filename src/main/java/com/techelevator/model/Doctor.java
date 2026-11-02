package com.techelevator.model;

import java.util.ArrayList;
import java.util.List;

public class Doctor {
    private int doctorId;
    private User user;
    private String firstName;
    private String lastName;
    private String specialty;
    private String phone;
    private List<String> availableDays = new ArrayList<>();
    private String availableTime;

    public Doctor() {
    }

    public Doctor(int doctorId, User user, String firstName, String lastName, String specialty, String phone,
                  List<String> availableDays, String availableTime) {
        this.doctorId = doctorId;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialty = specialty;
        this.phone = phone;
        this.availableDays = availableDays;
        this.availableTime = availableTime;
    }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
