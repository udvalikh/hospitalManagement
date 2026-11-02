package com.techelevator.dao;

import com.techelevator.model.Doctor;
import com.techelevator.model.DoctorDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.techelevator.dao.DaoSupport.blankToNull;
import static com.techelevator.dao.DaoSupport.execute;

@Component
public class JdbcDoctorDao implements DoctorDao {

    private static final String SELECT_DOCTOR =
            "SELECT d.doctor_id, d.first_name, d.last_name, d.specialty, d.phone, d.available_days, " +
            "d.available_time, u.user_id, u.username, u.role " +
            "FROM doctors d JOIN users u ON u.user_id = d.user_id ";

    private static final RowMapper<Doctor> DOCTOR_MAPPER = JdbcDoctorDao::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public JdbcDoctorDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Doctor getDoctorById(int doctorId) {
        return execute(() -> jdbcTemplate.query(SELECT_DOCTOR + "WHERE d.doctor_id = ?", DOCTOR_MAPPER, doctorId)
                .stream().findFirst().orElse(null));
    }

    @Override
    public Doctor getDoctorByUserId(int userId) {
        return execute(() -> jdbcTemplate.query(SELECT_DOCTOR + "WHERE d.user_id = ?", DOCTOR_MAPPER, userId)
                .stream().findFirst().orElse(null));
    }

    @Override
    public List<Doctor> getDoctors(String specialty) {
        String order = "ORDER BY d.last_name, d.first_name, d.doctor_id";
        if (specialty == null || specialty.isBlank()) {
            return execute(() -> jdbcTemplate.query(SELECT_DOCTOR + order, DOCTOR_MAPPER));
        }
        String pattern = "%" + escapeLike(specialty.trim()) + "%";
        return execute(() -> jdbcTemplate.query(SELECT_DOCTOR + "WHERE d.specialty ILIKE ? " + order,
                DOCTOR_MAPPER, pattern));
    }

    @Override
    public List<String> getSpecialties() {
        return execute(() -> jdbcTemplate.queryForList(
                "SELECT DISTINCT specialty FROM doctors WHERE specialty IS NOT NULL AND specialty <> '' " +
                "ORDER BY specialty", String.class));
    }

    @Override
    public Doctor createDoctor(int userId, DoctorDto doctor) {
        String sql = "INSERT INTO doctors (user_id, first_name, last_name, specialty, phone, available_days, available_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING doctor_id";
        Integer newId = execute(() -> jdbcTemplate.queryForObject(sql, Integer.class,
                userId,
                doctor.getFirstName().trim(),
                doctor.getLastName().trim(),
                blankToNull(doctor.getSpecialty()),
                blankToNull(doctor.getPhone()),
                joinDays(doctor.getAvailableDays()),
                blankToNull(doctor.getAvailableTime())));
        return getDoctorById(newId);
    }

    @Override
    public Doctor updateDoctor(int doctorId, DoctorDto doctor) {
        String sql = "UPDATE doctors SET first_name = ?, last_name = ?, specialty = ?, phone = ?, " +
                "available_days = ?, available_time = ? WHERE doctor_id = ?";
        int updated = execute(() -> jdbcTemplate.update(sql,
                doctor.getFirstName().trim(),
                doctor.getLastName().trim(),
                blankToNull(doctor.getSpecialty()),
                blankToNull(doctor.getPhone()),
                joinDays(doctor.getAvailableDays()),
                blankToNull(doctor.getAvailableTime()),
                doctorId));
        return updated == 1 ? getDoctorById(doctorId) : null;
    }

    @Override
    public boolean deleteDoctor(int doctorId) {
        // Deleting the user cascades to the doctor row and their appointments.
        String sql = "DELETE FROM users WHERE user_id = (SELECT user_id FROM doctors WHERE doctor_id = ?)";
        return execute(() -> jdbcTemplate.update(sql, doctorId)) == 1;
    }

    static String joinDays(List<String> days) {
        if (days == null || days.isEmpty()) {
            return null;
        }
        String joined = days.stream()
                .filter(day -> day != null && !day.isBlank())
                .map(day -> day.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.joining(","));
        return joined.isEmpty() ? null : joined;
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static Doctor mapRow(ResultSet rs, int rowNum) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getInt("doctor_id"));
        doctor.setUser(DaoSupport.mapEmbeddedUser(rs));
        doctor.setFirstName(rs.getString("first_name"));
        doctor.setLastName(rs.getString("last_name"));
        doctor.setSpecialty(rs.getString("specialty"));
        doctor.setPhone(rs.getString("phone"));
        String availableDays = rs.getString("available_days");
        doctor.setAvailableDays(availableDays == null || availableDays.isBlank()
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.stream(availableDays.split(",")).map(String::trim).toList()));
        doctor.setAvailableTime(rs.getString("available_time"));
        return doctor;
    }
}
