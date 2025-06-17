package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Doctor;
import com.techelevator.model.DoctorDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcDoctorDao implements DoctorDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcDoctorDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Doctor getDoctorById(int id) {
        Doctor output = new Doctor();
        String sql = "Select * from doctors where id = ?";

        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, id);
            if(result.next()) {
                output = mapRowToDoctor(result);
            }
        } catch (DaoException e){
            throw new DaoException("CAN'T GET doctor id : "+ id, e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    @Override
    public List<Doctor> getAllDoctors() {
        List<Doctor> output= new ArrayList<>();
        String sql= "select * from doctors";

        try{
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Doctor temp = mapRowToDoctor(results);
                output.add(temp);
            }
        } catch (DaoException e){
            throw new DaoException("can't get doctors", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    @Override
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        List<Doctor> output = new ArrayList<>();
        String sql = "select * from doctors where specialty like ?;";
        try{
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, specialty);
            while (results.next()){
                Doctor temp = mapRowToDoctor(results);
                output.add(temp);
            }
        } catch (DaoException e) {
            throw new DaoException("can't get doctors", e);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    @Override
    public Doctor createDoctor(DoctorDto dataIn) {

        String sql="insert into doctors (id, user_id, first_name, last_name, specialty, phone, available_days, available_time) +" +
                "values (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        Doctor doctor = mapFromDto(dataIn);
        try{
            int generatedId = jdbcTemplate.queryForObject(sql, Integer.class, doctor.getId(), doctor.getFirstName(), doctor.getLastName(), doctor.getSpecialty(), doctor.getPhone(), String.join(",", doctor.getAvailableDays()), doctor.getAvailableTime());
            doctor.setId(generatedId);

            return doctor;
        }catch (DaoException e) {
            throw new DaoException("can't get doctors", e);
        }

    }

    @Override
    public Doctor updateDoctor(DoctorDto dataIn) {
        String sql = "UPDATE doctors SET user_id = ?, first_name = ?, last_name = ?, specialty = ?, phone = ?, " +
                "available_days = ?, available_time = ? " +  // Added missing space before WHERE
                "WHERE id = ?;";
        Doctor doctor = mapFromDto(dataIn);

        try {
            int rowsAffected = jdbcTemplate.update(
                    sql,
                    doctor.getUserId(),
                    doctor.getFirstName(),
                    doctor.getLastName(),
                    doctor.getSpecialty(),
                    doctor.getPhone(),
                    String.join(",", doctor.getAvailableDays()),
                    doctor.getAvailableTime(),
                    doctor.getId()
            );

            if (rowsAffected == 1) {
                // Optional: re-fetch from DB if needed for freshest data
                return doctor;  // or fetchDoctorById(doctor.getId());
            } else {
                throw new DaoException("No doctor found with ID: " + doctor.getId());
            }

        } catch (DaoException e) {
            throw new DaoException("Failed to update doctor", e);
        } catch (Exception e) {
            throw new RuntimeException("Database error while updating doctor", e);
        }

    }

    @Override
    public boolean deleteDoctor(int id) {
        return false;
    }

    private Doctor mapRowToDoctor(SqlRowSet input) throws SQLException {
        Doctor doctor = new Doctor();

        doctor.setId(input.getInt("id"));
        doctor.setUserId(input.getInt("user_id"));
        doctor.setFirstName(input.getString("first_name"));
        doctor.setLastName(input.getString("last_name"));
        doctor.setSpecialty(input.getString("specialty"));
        doctor.setPhone(input.getString("phone"));
        doctor.setAvailableDays(input.getString("available_days"));
        doctor.setAvailableTime(input.getString("available_time"));

        return doctor;
    }

    private Doctor mapFromDto(DoctorDto dto){
        Doctor doctor = new Doctor();
        doctor.setId(dto.getId());
        doctor.setUserId(dto.getUserId());
        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setPhone(dto.getPhone());
        doctor.setAvailableDays(dto.getAvailableDays());
        doctor.setAvailableTime(dto.getAvailableTime());

        return doctor;
    }
}
