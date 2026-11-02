package com.techelevator.model;

public class Patient {
    private int patientId;
    private User user;
    private String firstName;
    private String lastName;
    private String insuranceNumber;
    private String phone;
    private String email;

    public Patient() {
    }

    public Patient(int patientId, User user, String firstName, String lastName, String insuranceNumber,
                   String phone, String email) {
        this.patientId = patientId;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.insuranceNumber = insuranceNumber;
        this.phone = phone;
        this.email = email;
    }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
