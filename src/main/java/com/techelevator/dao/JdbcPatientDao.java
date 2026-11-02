package com.techelevator.dao;

import com.techelevator.model.Patient;
import com.techelevator.model.PatientDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.techelevator.dao.DaoSupport.blankToNull;
import static com.techelevator.dao.DaoSupport.execute;

@Component
public class JdbcPatientDao implements PatientDao {

    private static final String SELECT_PATIENT =
            "SELECT p.patient_id, p.first_name, p.last_name, p.insurance_number, p.phone, p.email, " +
            "u.user_id, u.username, u.role " +
            "FROM patients p JOIN users u ON u.user_id = p.user_id ";

    private static final RowMapper<Patient> PATIENT_MAPPER = JdbcPatientDao::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public JdbcPatientDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Patient getPatientById(int patientId) {
        return execute(() -> jdbcTemplate.query(SELECT_PATIENT + "WHERE p.patient_id = ?", PATIENT_MAPPER, patientId)
                .stream().findFirst().orElse(null));
    }

    @Override
    public Patient getPatientByUserId(int userId) {
        return execute(() -> jdbcTemplate.query(SELECT_PATIENT + "WHERE p.user_id = ?", PATIENT_MAPPER, userId)
                .stream().findFirst().orElse(null));
    }

    @Override
    public List<Patient> getPatients() {
        return execute(() -> jdbcTemplate.query(
                SELECT_PATIENT + "ORDER BY p.last_name NULLS LAST, p.first_name NULLS LAST, p.patient_id",
                PATIENT_MAPPER));
    }

    @Override
    public Patient createPatient(int userId, PatientDto patient) {
        String sql = "INSERT INTO patients (user_id, first_name, last_name, insurance_number, phone, email) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING patient_id";
        Integer newId = execute(() -> jdbcTemplate.queryForObject(sql, Integer.class,
                userId,
                blankToNull(patient.getFirstName()),
                blankToNull(patient.getLastName()),
                blankToNull(patient.getInsuranceNumber()),
                blankToNull(patient.getPhone()),
                normalizeEmail(patient.getEmail())));
        return getPatientById(newId);
    }

    @Override
    public Patient updatePatient(int patientId, PatientDto patient) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, insurance_number = ?, phone = ?, email = ? " +
                "WHERE patient_id = ?";
        int updated = execute(() -> jdbcTemplate.update(sql,
                blankToNull(patient.getFirstName()),
                blankToNull(patient.getLastName()),
                blankToNull(patient.getInsuranceNumber()),
                blankToNull(patient.getPhone()),
                normalizeEmail(patient.getEmail()),
                patientId));
        return updated == 1 ? getPatientById(patientId) : null;
    }

    @Override
    public boolean deletePatient(int patientId) {
        // Deleting the user cascades to the patient row and their appointments.
        String sql = "DELETE FROM users WHERE user_id = (SELECT user_id FROM patients WHERE patient_id = ?)";
        return execute(() -> jdbcTemplate.update(sql, patientId)) == 1;
    }

    private static String normalizeEmail(String email) {
        String value = blankToNull(email);
        return value == null ? null : value.toLowerCase();
    }

    private static Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setUser(DaoSupport.mapEmbeddedUser(rs));
        patient.setFirstName(rs.getString("first_name"));
        patient.setLastName(rs.getString("last_name"));
        patient.setInsuranceNumber(rs.getString("insurance_number"));
        patient.setPhone(rs.getString("phone"));
        patient.setEmail(rs.getString("email"));
        return patient;
    }
}
