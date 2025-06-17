package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentUpdateDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcAppointmentDao implements AppointmentDao{

    private JdbcTemplate jdbcTemplate;

    public JdbcAppointmentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Appointment createAppointment(AppointmentRequestDto dataIn) {

        String sql = "INSERT INTO appointments (id, doctor_id, patient_id, appointment_date, reason) VALUES (?, ?, ?, ?, ?, ?)";

        try{
            Long generatedId = jdbcTemplate.queryForObject(sql, Long.class, dataIn.getDoctorId(), dataIn.getPatientId(),
                    Timestamp.valueOf(dataIn.getAppointmentDate()), dataIn.getReason());
            if (generatedId != null) {
                // Re-fetch the created appointment
                String selectSql = "SELECT * FROM appointments WHERE id = ?";
                SqlRowSet result = jdbcTemplate.queryForRowSet(selectSql, generatedId);

                if (result.next()) {
                    return mapRowToAppointment(result);
                } else {
                    throw new DaoException("Appointment created but not found.");
                }
            } else {
                throw new DaoException("Failed to create appointment.");
            }
        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }
    }

    @Override
    public Appointment getAppointmentById(int id) {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, id);
            if (result.next()) {
                return mapRowToAppointment(result);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching appointment by id", e);
        } catch (DaoException e) {
            throw new DaoException("Failed to get appointment by id: " + id, e);
        }
        throw new DaoException("Appointment with id " + id + " not found.");
    }

    @Override
    public List<Appointment> getAllAppointments() {
        List<Appointment> output = new ArrayList<>();
        String sql = "select * from appointments";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql);
            while(result.next()){
                Appointment appointment = mapRowToAppointment(result);
                output.add(appointment);
           }
        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }
        return output;
    }

    @Override
    public Appointment updateAppointment(AppointmentUpdateDto dto) {
        String sql = "UPDATE appointments SET doctor_id = ?, patient_id = ?, appointment_date = ?, reason = ? WHERE id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(
                    sql,
                    dto.getDoctorId(),
                    dto.getPatientId(),
                    Timestamp.valueOf(dto.getAppointmentDate()),
                    dto.getReason(),
                    dto.getId()
            );

            if (rowsAffected == 1) {
                // Re-fetch the updated appointment
                String selectSql = "SELECT * FROM appointments WHERE id = ?";
                SqlRowSet result = jdbcTemplate.queryForRowSet(selectSql, dto.getId());

                if (result.next()) {
                    return mapRowToAppointment(result);
                } else {
                    throw new DaoException("Appointment updated but not found.");
                }
            } else {
                throw new DaoException("No appointment found with id " + dto.getId());
            }

        } catch (DaoException e) {
            throw new DaoException("Failed to update appointment", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating appointment", e);
        }
    }

    @Override
    public boolean deleteAppointment(int id) {
        String sql = "delete from appointments where id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, id);
            return rowsAffected == 1;
        } catch (DaoException e) {
            throw new DaoException("Failed to update doctor", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating doctor", e);
        }
    }

    @Override
    public List<Appointment> getAppointmentsByPatientId(int patientId) {
        List<Appointment> output = new ArrayList<>();
        String sql = "select * from appointments where patient_id = ?";

        try{
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, patientId);
            while(result.next()){
                Appointment appointment = mapRowToAppointment(result);
                output.add(appointment);
            }
        }catch (DaoException e) {
            throw new DaoException("Failed to update doctor", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating doctor", e);
        }
        return output;
    }

    @Override
    public List<Appointment> getAppointmentsByDoctorId(int doctorId) {
        List<Appointment> output = new ArrayList<>();
        String sql = "Select * from appointments where doctor_id = ?";
        try{
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, doctorId);
            while(result.next()){
                Appointment appointment = mapRowToAppointment(result);
                output.add(appointment);
            }
        } catch (DaoException e) {
            throw new DaoException("Failed to update doctor", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating doctor", e);
        }
        return output;
    }

    @Override
    public List<Appointment> getAppointmentsByDate(LocalDateTime date) {
        List<Appointment> output = new ArrayList<>();
        String sql = "select * from appointments where appointment_date = ?";
        try{
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, date);
            while(result.next()){
                Appointment appointment = mapRowToAppointment(result);
                output.add(appointment);

            }
        } catch (DaoException e) {
            throw new DaoException("Failed to update doctor", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating doctor", e);
        }
        return output;
    }

    @Override
    public boolean isDoctorAvailable(int doctorId, LocalDateTime date) {
        String sql = "select count(*) from appointments where doctor_id= ? and appointment_date = ?";

        int count = jdbcTemplate.queryForObject(sql, Integer.class, doctorId, date);
        return count ==0;
    }

    private Appointment mapRowToAppointment(SqlRowSet input) throws SQLException {

        Appointment appointment = new Appointment();

        appointment.setId(input.getInt("id"));
        appointment.setDoctorId(input.getInt("doctors_id"));
        appointment.setPatientId(input.getInt("patient_id"));
        appointment.setAppointmentDate(input.getTimestamp("appointment_date").toLocalDateTime());
        appointment.setReason(input.getString("reason"));

        return appointment;
    }
}
