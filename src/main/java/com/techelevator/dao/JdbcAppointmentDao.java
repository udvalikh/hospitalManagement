package com.techelevator.dao;

import com.techelevator.model.Appointment;
import com.techelevator.model.AppointmentRequestDto;
import com.techelevator.model.AppointmentStatus;
import com.techelevator.model.AppointmentUpdateDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.techelevator.dao.DaoSupport.blankToNull;
import static com.techelevator.dao.DaoSupport.execute;

@Component
public class JdbcAppointmentDao implements AppointmentDao {

    private static final String SELECT_APPOINTMENT =
            "SELECT a.appointment_id, a.doctor_id, a.patient_id, a.appointment_date, a.status, a.reason, a.created_at, " +
            "CONCAT_WS(' ', d.first_name, d.last_name) AS doctor_name, d.specialty AS doctor_specialty, " +
            "NULLIF(CONCAT_WS(' ', p.first_name, p.last_name), '') AS patient_name " +
            "FROM appointments a " +
            "JOIN doctors d ON d.doctor_id = a.doctor_id " +
            "JOIN patients p ON p.patient_id = a.patient_id ";

    private static final RowMapper<Appointment> APPOINTMENT_MAPPER = JdbcAppointmentDao::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public JdbcAppointmentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Appointment getAppointmentById(int appointmentId) {
        return execute(() -> jdbcTemplate.query(SELECT_APPOINTMENT + "WHERE a.appointment_id = ?",
                APPOINTMENT_MAPPER, appointmentId).stream().findFirst().orElse(null));
    }

    @Override
    public List<Appointment> getAppointments(Integer doctorId, Integer patientId, LocalDate date,
                                             AppointmentStatus status) {
        StringBuilder sql = new StringBuilder(SELECT_APPOINTMENT).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (doctorId != null) {
            sql.append("AND a.doctor_id = ? ");
            params.add(doctorId);
        }
        if (patientId != null) {
            sql.append("AND a.patient_id = ? ");
            params.add(patientId);
        }
        if (date != null) {
            sql.append("AND a.appointment_date >= ? AND a.appointment_date < ? ");
            params.add(Timestamp.valueOf(date.atStartOfDay()));
            params.add(Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
        }
        if (status != null) {
            sql.append("AND a.status = ? ");
            params.add(status.name());
        }
        sql.append("ORDER BY a.appointment_date, a.appointment_id");
        return execute(() -> jdbcTemplate.query(sql.toString(), APPOINTMENT_MAPPER, params.toArray()));
    }

    @Override
    public Appointment createAppointment(AppointmentRequestDto appointment) {
        String sql = "INSERT INTO appointments (doctor_id, patient_id, appointment_date, reason) " +
                "VALUES (?, ?, ?, ?) RETURNING appointment_id";
        Integer newId = execute(() -> jdbcTemplate.queryForObject(sql, Integer.class,
                appointment.getDoctorId(),
                appointment.getPatientId(),
                Timestamp.valueOf(appointment.getAppointmentDate()),
                blankToNull(appointment.getReason())));
        return getAppointmentById(newId);
    }

    @Override
    public Appointment updateAppointment(int appointmentId, AppointmentUpdateDto appointment) {
        String sql = "UPDATE appointments SET doctor_id = ?, appointment_date = ?, reason = ? WHERE appointment_id = ?";
        int updated = execute(() -> jdbcTemplate.update(sql,
                appointment.getDoctorId(),
                Timestamp.valueOf(appointment.getAppointmentDate()),
                blankToNull(appointment.getReason()),
                appointmentId));
        return updated == 1 ? getAppointmentById(appointmentId) : null;
    }

    @Override
    public Appointment updateStatus(int appointmentId, AppointmentStatus status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        int updated = execute(() -> jdbcTemplate.update(sql, status.name(), appointmentId));
        return updated == 1 ? getAppointmentById(appointmentId) : null;
    }

    @Override
    public boolean deleteAppointment(int appointmentId) {
        return execute(() -> jdbcTemplate.update("DELETE FROM appointments WHERE appointment_id = ?", appointmentId)) == 1;
    }

    @Override
    public boolean isDoctorAvailable(int doctorId, LocalDateTime dateTime, Integer excludeAppointmentId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? " +
                "AND status <> 'CANCELLED' AND appointment_id <> ?";
        Integer count = execute(() -> jdbcTemplate.queryForObject(sql, Integer.class,
                doctorId,
                Timestamp.valueOf(dateTime),
                excludeAppointmentId == null ? -1 : excludeAppointmentId));
        return count == null || count == 0;
    }

    @Override
    public List<LocalDateTime> getBookedTimes(int doctorId, LocalDate date) {
        String sql = "SELECT appointment_date FROM appointments WHERE doctor_id = ? " +
                "AND appointment_date >= ? AND appointment_date < ? AND status <> 'CANCELLED' " +
                "ORDER BY appointment_date";
        return execute(() -> jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getTimestamp("appointment_date").toLocalDateTime(),
                doctorId,
                Timestamp.valueOf(date.atStartOfDay()),
                Timestamp.valueOf(date.plusDays(1).atStartOfDay())));
    }

    private static Appointment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appointment_id"));
        appointment.setDoctorId(rs.getInt("doctor_id"));
        appointment.setPatientId(rs.getInt("patient_id"));
        Timestamp date = rs.getTimestamp("appointment_date");
        appointment.setAppointmentDate(date != null ? date.toLocalDateTime() : null);
        appointment.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        appointment.setReason(rs.getString("reason"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        appointment.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        appointment.setDoctorName(rs.getString("doctor_name"));
        appointment.setDoctorSpecialty(rs.getString("doctor_specialty"));
        appointment.setPatientName(rs.getString("patient_name"));
        return appointment;
    }
}
