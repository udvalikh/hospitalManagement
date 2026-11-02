package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

/** Shared helpers for the JDBC DAOs. */
final class DaoSupport {

    private DaoSupport() {
    }

    /**
     * Runs a database operation and translates the failures callers care about into {@link DaoException}.
     * A {@link DataIntegrityViolationException} cause is surfaced to clients as HTTP 409.
     */
    static <T> T execute(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
    }

    /** Builds the (password-less) user embedded in doctor and patient rows. */
    static User mapEmbeddedUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        String role = rs.getString("role");
        if (role != null) {
            user.setAuthorities(role);
        }
        user.setActivated(true);
        return user;
    }

    /** Converts empty strings to null so optional columns stay NULL instead of "". */
    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
