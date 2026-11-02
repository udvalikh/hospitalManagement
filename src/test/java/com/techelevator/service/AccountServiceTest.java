package com.techelevator.service;

import com.techelevator.dao.DoctorDao;
import com.techelevator.dao.PatientDao;
import com.techelevator.dao.UserDao;
import com.techelevator.exception.BadRequestException;
import com.techelevator.exception.ConflictException;
import com.techelevator.model.CreateDoctorDto;
import com.techelevator.model.PatientDto;
import com.techelevator.model.RegisterUserDto;
import com.techelevator.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private UserDao userDao;
    private DoctorDao doctorDao;
    private PatientDao patientDao;
    private AccountService sut;

    @BeforeEach
    void setup() {
        userDao = mock(UserDao.class);
        doctorDao = mock(DoctorDao.class);
        patientDao = mock(PatientDao.class);
        sut = new AccountService(userDao, doctorDao, patientDao);
        when(userDao.createUser(any())).thenReturn(new User(42, "newbie", "hash", "ROLE_PATIENT"));
    }

    private static RegisterUserDto registration(String role) {
        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("newbie");
        dto.setPassword("password1");
        dto.setConfirmPassword("password1");
        dto.setRole(role);
        dto.setFirstName("New");
        dto.setEmail("new@example.com");
        return dto;
    }

    @Test
    void registerPatient_ignores_requested_role_and_creates_profile() {
        sut.registerPatient(registration("ROLE_ADMIN"));

        ArgumentCaptor<RegisterUserDto> account = ArgumentCaptor.forClass(RegisterUserDto.class);
        verify(userDao).createUser(account.capture());
        assertEquals(AccountService.ROLE_PATIENT, account.getValue().getRole());

        ArgumentCaptor<PatientDto> profile = ArgumentCaptor.forClass(PatientDto.class);
        verify(patientDao).createPatient(eq(42), profile.capture());
        assertEquals("New", profile.getValue().getFirstName());
        assertEquals("new@example.com", profile.getValue().getEmail());
    }

    @Test
    void registerPatient_password_mismatch_is_rejected() {
        RegisterUserDto dto = registration(null);
        dto.setConfirmPassword("different");

        assertThrows(BadRequestException.class, () -> sut.registerPatient(dto));
        verify(userDao, never()).createUser(any());
    }

    @Test
    void registerPatient_taken_username_is_conflict() {
        when(userDao.getUserByUsername("newbie")).thenReturn(new User());

        assertThrows(ConflictException.class, () -> sut.registerPatient(registration(null)));
        verify(patientDao, never()).createPatient(anyInt(), any());
    }

    @Test
    void createDoctor_creates_doctor_account_and_profile() {
        CreateDoctorDto dto = new CreateDoctorDto();
        dto.setUsername("drnew");
        dto.setPassword("password1");
        dto.setFirstName("Dr");
        dto.setLastName("New");

        sut.createDoctor(dto);

        ArgumentCaptor<RegisterUserDto> account = ArgumentCaptor.forClass(RegisterUserDto.class);
        verify(userDao).createUser(account.capture());
        assertEquals(AccountService.ROLE_DOCTOR, account.getValue().getRole());
        verify(doctorDao).createDoctor(42, dto);
    }
}
