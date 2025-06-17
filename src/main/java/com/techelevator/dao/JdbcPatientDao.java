package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Patient;
import com.techelevator.model.PatientDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPatientDao implements PatientDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPatientDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Patient createPatient(PatientDto dataIn) {
        String sql = "INSERT INTO patients (id, user_id, first_name, last_name, insurance_number, email, phone) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try {
            int newId = jdbcTemplate.queryForObject(sql, Integer.class, dataIn.getId(), dataIn.getFirstName(), dataIn.getLastName(), dataIn.getInsuranceNumber(), dataIn.getEmail(), dataIn.getPhone());
            return new Patient(newId, dataIn.getUserId(), dataIn.getFirstName(), dataIn.getLastName(), dataIn.getInsuranceNumber(), dataIn.getPhone(), dataIn.getEmail());
        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }

    }

    @Override
    public Optional<Patient> findById(Long id) {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try {
            Patient patient = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Patient(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("insurance_number"),
                    rs.getString("phone"),
                    rs.getString("email")
            ), id);
            return Optional.ofNullable(patient);
        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }

    }

    @Override
    public List<Patient> findAll() {
        List<Patient> output = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                output.add(mapRowToPatient(results));
            }
            return output;
        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }
    }

    @Override
    public Patient updatePatient(PatientDto dataIn) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, insurance_number = ?, email = ?, phone = ? WHERE id = ?";

        try {
            jdbcTemplate.update(sql, dataIn.getFirstName(), dataIn.getLastName(), dataIn.getInsuranceNumber(), dataIn.getEmail(), dataIn.getPhone(), dataIn.getId());
            return new Patient(dataIn.getId(), dataIn.getUserId(), dataIn.getFirstName(), dataIn.getLastName(), dataIn.getInsuranceNumber(), dataIn.getPhone(), dataIn.getEmail());
        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM patients WHERE id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, id);
            return rowsAffected == 1;
        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }
    }

    @Override
    public Optional<Patient> findByEmail(String email) {
        String sql = "SELECT * FROM patients WHERE email = ?";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, email);
            if (result.next()) {
                return Optional.of(mapRowToPatient(result));
            }
        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }
        return Optional.empty();
    }

    private Patient mapRowToPatient(SqlRowSet input) {
        Patient patient = new Patient();
        patient.setId(input.getInt("id"));
        patient.setUserId(input.getInt("user_id"));
        patient.setFirstName(input.getString("first_name"));
        patient.setLastName(input.getString("last_name"));
        patient.setInsuranceNumber(input.getString("insurance_number"));
        patient.setPhone(input.getString("phone"));
        patient.setEmail(input.getString("email"));
        return patient;
    }

}



