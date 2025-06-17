package com.techelevator.model;

public class PatientDto {
    private int id;
    private int userId;
    private String firstName;
    private String lastName;
    private String insuranceNumber;
    private String phone;
    private String email;

    public PatientDto() {
    }

    public PatientDto(int id, int userId, String firstName, String lastName, String insuranceNumber, String phone, String email) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.insuranceNumber = insuranceNumber;
        this.phone = phone;
        this.email = email;
    }

    public int getId() {
        return id;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
