package com.techelevator.model;

public class DoctorDto {
    private int id;
    private int userId;
    private String firstName;
    private String lastName;
    private String specialty;
    private String phone;
    private String availableDays; // or List<String>
    private String availableTime;

    public DoctorDto(){

    }

    public DoctorDto(Integer id, int userId, String name, String lastName, String specialization, String availableDays, String availableTime, String phone) {
        this.id = id;
        this.userId = userId;
        this.firstName = name;
        this.lastName = lastName;
        this.specialty = specialization;
        this.availableDays = availableDays;
        this.availableTime = availableTime;
        this.phone = phone;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
