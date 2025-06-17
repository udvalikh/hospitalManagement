package com.techelevator.model;

public class Doctor {
    private int id;
    private int userId;
    private String firstName;
    private String lastName;
    private String specialty;
    private String phone;
    private String availableDays;
    private String availableTime;

    public Doctor(int id, String lastName, String firstName, String specialty, int userId, String phone, String availableDays, String availableTime) {
        this.id = id;
        this.firstName=firstName;
        this.lastName=lastName;
        this.specialty = specialty;
        this.userId = userId;
        this.phone = phone;
        this.availableDays = availableDays;
        this.availableTime = availableTime;
    }

    public Doctor() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }
}
