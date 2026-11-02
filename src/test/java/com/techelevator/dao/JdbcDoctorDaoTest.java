package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Doctor;
import com.techelevator.model.DoctorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcDoctorDaoTest extends BaseDaoTest {

    private JdbcDoctorDao sut;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcDoctorDao(jdbcTemplate);
    }

    @Test
    public void getDoctorById_returns_doctor_with_user_and_schedule() {
        Doctor doctor = sut.getDoctorById(1);

        assertNotNull(doctor);
        assertEquals("Alice", doctor.getFirstName());
        assertEquals("Adams", doctor.getLastName());
        assertEquals("Cardiology", doctor.getSpecialty());
        assertEquals(List.of("MONDAY", "WEDNESDAY"), doctor.getAvailableDays());
        assertEquals("09:00-12:00", doctor.getAvailableTime());
        assertEquals(4, doctor.getUser().getUserId());
        assertEquals("doctor1", doctor.getUser().getUsername());
        assertEquals("ROLE_DOCTOR", doctor.getUser().getRole());
        assertNull(doctor.getUser().getPassword(), "password hash must not be loaded");
    }

    @Test
    public void getDoctorById_with_null_schedule_returns_empty_days() {
        Doctor doctor = sut.getDoctorById(2);

        assertTrue(doctor.getAvailableDays().isEmpty());
        assertNull(doctor.getAvailableTime());
    }

    @Test
    public void getDoctorById_missing_returns_null() {
        assertNull(sut.getDoctorById(999));
    }

    @Test
    public void getDoctorByUserId_returns_linked_doctor() {
        assertEquals(2, sut.getDoctorByUserId(5).getDoctorId());
        assertNull(sut.getDoctorByUserId(1));
    }

    @Test
    public void getDoctors_without_filter_returns_all_sorted_by_last_name() {
        List<Doctor> doctors = sut.getDoctors(null);

        assertEquals(2, doctors.size());
        assertEquals("Adams", doctors.get(0).getLastName());
        assertEquals("Brown", doctors.get(1).getLastName());
    }

    @Test
    public void getDoctors_filters_by_partial_case_insensitive_specialty() {
        List<Doctor> doctors = sut.getDoctors("cardi");

        assertEquals(1, doctors.size());
        assertEquals(1, doctors.get(0).getDoctorId());
    }

    @Test
    public void getDoctors_treats_like_wildcards_literally() {
        assertTrue(sut.getDoctors("%").isEmpty());
    }

    @Test
    public void getSpecialties_returns_distinct_sorted() {
        assertEquals(List.of("Cardiology", "Pediatrics"), sut.getSpecialties());
    }

    @Test
    public void createDoctor_inserts_and_returns_doctor() {
        Doctor created = sut.createDoctor(1, new DoctorDto("Eve", "Evans", "Dermatology", "555-3000",
                List.of("tuesday", " Friday "), "08:00-12:00"));

        assertTrue(created.getDoctorId() > 0);
        assertEquals("Eve", created.getFirstName());
        assertEquals(List.of("TUESDAY", "FRIDAY"), created.getAvailableDays());
        assertEquals("user1", created.getUser().getUsername());
        assertNotNull(sut.getDoctorById(created.getDoctorId()));
    }

    @Test
    public void createDoctor_for_user_that_is_already_a_doctor_throws() {
        DoctorDto dto = new DoctorDto("Dup", "Licate", null, null, List.of(), null);
        assertThrows(DaoException.class, () -> sut.createDoctor(4, dto));
    }

    @Test
    public void updateDoctor_updates_fields() {
        Doctor updated = sut.updateDoctor(1, new DoctorDto("Alicia", "Adams", "Neurology", "555-9999",
                List.of("FRIDAY"), "13:00-17:00"));

        assertEquals("Alicia", updated.getFirstName());
        assertEquals("Neurology", updated.getSpecialty());
        assertEquals(List.of("FRIDAY"), updated.getAvailableDays());
        assertEquals("13:00-17:00", sut.getDoctorById(1).getAvailableTime());
    }

    @Test
    public void updateDoctor_missing_returns_null() {
        assertNull(sut.updateDoctor(999, new DoctorDto("A", "B", null, null, List.of(), null)));
    }

    @Test
    public void deleteDoctor_removes_doctor_user_and_appointments() {
        assertTrue(sut.deleteDoctor(1));

        assertNull(sut.getDoctorById(1));
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = 4", Integer.class));
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointments WHERE doctor_id = 1", Integer.class));
    }

    @Test
    public void deleteDoctor_missing_returns_false() {
        assertFalse(sut.deleteDoctor(999));
    }
}
