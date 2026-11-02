package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Patient;
import com.techelevator.model.PatientDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcPatientDaoTest extends BaseDaoTest {

    private JdbcPatientDao sut;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcPatientDao(jdbcTemplate);
    }

    @Test
    public void getPatientById_returns_patient_with_user() {
        Patient patient = sut.getPatientById(1);

        assertEquals("Carol", patient.getFirstName());
        assertEquals("Clark", patient.getLastName());
        assertEquals("INS-1", patient.getInsuranceNumber());
        assertEquals("carol@example.com", patient.getEmail());
        assertEquals("patient1", patient.getUser().getUsername());
        assertEquals("ROLE_PATIENT", patient.getUser().getRole());
    }

    @Test
    public void getPatientById_missing_returns_null() {
        assertNull(sut.getPatientById(999));
    }

    @Test
    public void getPatientByUserId_returns_linked_patient() {
        assertEquals(2, sut.getPatientByUserId(7).getPatientId());
        assertNull(sut.getPatientByUserId(4));
    }

    @Test
    public void getPatients_returns_all() {
        List<Patient> patients = sut.getPatients();

        assertEquals(2, patients.size());
        assertEquals("Clark", patients.get(0).getLastName());
        assertEquals("Davis", patients.get(1).getLastName());
    }

    @Test
    public void createPatient_with_minimal_profile() {
        Patient created = sut.createPatient(1, new PatientDto(null, "  ", null, null, null));

        assertTrue(created.getPatientId() > 0);
        assertNull(created.getLastName(), "blank strings are stored as NULL");
        assertEquals(1, created.getUser().getUserId());
    }

    @Test
    public void createPatient_lowercases_email() {
        Patient created = sut.createPatient(2, new PatientDto("Ann", "Lee", null, null, "Ann.Lee@Example.COM"));
        assertEquals("ann.lee@example.com", created.getEmail());
    }

    @Test
    public void createPatient_with_duplicate_email_throws_integrity_error() {
        DaoException e = assertThrows(DaoException.class,
                () -> sut.createPatient(1, new PatientDto("X", "Y", null, null, "carol@example.com")));
        assertInstanceOf(DataIntegrityViolationException.class, e.getCause());
    }

    @Test
    public void updatePatient_updates_fields() {
        Patient updated = sut.updatePatient(1, new PatientDto("Caroline", "Clark", "INS-9", "555-0000", "caroline@example.com"));

        assertEquals("Caroline", updated.getFirstName());
        assertEquals("INS-9", updated.getInsuranceNumber());
        assertEquals("caroline@example.com", sut.getPatientById(1).getEmail());
    }

    @Test
    public void updatePatient_missing_returns_null() {
        assertNull(sut.updatePatient(999, new PatientDto()));
    }

    @Test
    public void deletePatient_removes_patient_user_and_appointments() {
        assertTrue(sut.deletePatient(1));

        assertNull(sut.getPatientById(1));
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = 6", Integer.class));
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointments WHERE patient_id = 1", Integer.class));
    }

    @Test
    public void deletePatient_missing_returns_false() {
        assertFalse(sut.deletePatient(999));
    }
}
