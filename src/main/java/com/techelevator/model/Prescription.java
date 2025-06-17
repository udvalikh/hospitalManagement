package com.techelevator.model;

public class Prescription {
    private Long id;
    private Long medicalRecordId;
    private String medication;
    private String dosage;
    private String instructions;

    public Prescription() {
    }

    public Prescription(Long medicalRecordId, Long id, String medication, String dosage, String instructions) {
        this.medicalRecordId = medicalRecordId;
        this.id = id;
        this.medication = medication;
        this.dosage = dosage;
        this.instructions = instructions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(Long medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
