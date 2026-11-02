package com.techelevator.controller;

import com.techelevator.model.LoginDto;
import com.techelevator.model.LoginResponseDto;
import com.techelevator.model.Patient;
import com.techelevator.model.RegisterUserDto;
import com.techelevator.security.CurrentUser;
import com.techelevator.security.CurrentUserService;
import com.techelevator.security.jwt.JWTFilter;
import com.techelevator.security.jwt.TokenProvider;
import com.techelevator.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class AuthenticationController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final CurrentUserService currentUserService;
    private final AccountService accountService;

    public AuthenticationController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder,
                                    CurrentUserService currentUserService, AccountService accountService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.currentUserService = currentUserService;
        this.accountService = accountService;
    }

    /** Returns a JWT plus the user and their linked doctorId/patientId. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
        // Throws BadCredentialsException (-> 401) on a wrong username or password.
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(credentials);
        String jwt = tokenProvider.createToken(authentication, false);

        CurrentUser current = currentUserService.forUsername(authentication.getName());
        return withToken(HttpStatus.OK, jwt, current);
    }

    /** Patient self-registration. Responds 201 with a token so the frontend can sign the user in at once. */
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody RegisterUserDto newUser) {
        Patient patient = accountService.registerPatient(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(patient.getUser().getUsername(), null,
                List.of(new SimpleGrantedAuthority(AccountService.ROLE_PATIENT)));
        String jwt = tokenProvider.createToken(authentication, false);
        return withToken(HttpStatus.CREATED, jwt,
                new CurrentUser(patient.getUser(), null, patient.getPatientId()));
    }

    /** The signed-in user, for restoring a session from a stored token. */
    @GetMapping("/me")
    public LoginResponseDto me(Principal principal) {
        CurrentUser current = currentUserService.get(principal);
        return new LoginResponseDto(null, current.user(), current.doctorId(), current.patientId());
    }

    private static ResponseEntity<LoginResponseDto> withToken(HttpStatus status, String jwt, CurrentUser current) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        LoginResponseDto body = new LoginResponseDto(jwt, current.user(), current.doctorId(), current.patientId());
        return new ResponseEntity<>(body, headers, status);
    }
}
