package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.RegisterUserDto;
import com.techelevator.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcUserDaoTest extends BaseDaoTest {
    protected static final User USER_1 = new User(1, "user1", "user1", "ROLE_USER");
    protected static final User USER_2 = new User(2, "user2", "user2", "ROLE_USER");
    private static final User USER_3 = new User(3, "user3", "user3", "ROLE_USER");

    private JdbcUserDao sut;

    @BeforeEach
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcUserDao(jdbcTemplate);
    }

    @Test
    public void getUserByUsername_given_null_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> sut.getUserByUsername(null));
    }

    @Test
    public void getUserByUsername_given_invalid_username_returns_null() {
        assertNull(sut.getUserByUsername("invalid"));
    }

    @Test
    public void getUserByUsername_given_valid_user_returns_user() {
        assertEquals(USER_1, sut.getUserByUsername(USER_1.getUsername()));
    }

    @Test
    public void getUserByUsername_is_case_and_whitespace_insensitive() {
        assertEquals(USER_1, sut.getUserByUsername("  USER1 "));
    }

    @Test
    public void getUserById_given_invalid_user_id_returns_null() {
        assertNull(sut.getUserById(-1));
    }

    @Test
    public void getUserById_given_valid_user_id_returns_user() {
        assertEquals(USER_1, sut.getUserById(USER_1.getUserId()));
    }

    @Test
    public void getUsers_returns_all_users() {
        List<User> users = sut.getUsers();

        assertNotNull(users);
        assertEquals(8, users.size());
        assertEquals(USER_1, users.get(0));
        assertEquals(USER_2, users.get(1));
        assertEquals(USER_3, users.get(2));
    }

    @Test
    public void createUser_with_null_username() {
        RegisterUserDto registerUserDto = newRegistration(null, USER_3.getPassword());
        assertThrows(DaoException.class, () -> sut.createUser(registerUserDto));
    }

    @Test
    public void createUser_with_existing_username() {
        RegisterUserDto registerUserDto = newRegistration(USER_1.getUsername(), USER_3.getPassword());
        assertThrows(DaoException.class, () -> sut.createUser(registerUserDto));
    }

    @Test
    public void createUser_with_null_password() {
        RegisterUserDto registerUserDto = newRegistration("brandnew", null);
        assertThrows(IllegalArgumentException.class, () -> sut.createUser(registerUserDto));
    }

    @Test
    public void createUser_creates_a_user_with_hashed_password() {
        User createdUser = sut.createUser(newRegistration("new", "user"));

        assertNotNull(createdUser);
        assertNotEquals("user", createdUser.getPassword(), "password must be stored hashed");
        assertEquals(createdUser, sut.getUserByUsername(createdUser.getUsername()));
    }

    @Test
    public void createUser_normalizes_role_and_username() {
        RegisterUserDto registration = newRegistration("  MixedCase ", "password");
        registration.setRole("patient");

        User createdUser = sut.createUser(registration);

        assertEquals("mixedcase", createdUser.getUsername());
        assertEquals("ROLE_PATIENT", createdUser.getRole());
    }

    private static RegisterUserDto newRegistration(String username, String password) {
        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setRole("ROLE_USER");
        return dto;
    }
}
