package com.techelevator.controller;

import com.techelevator.exception.BadRequestException;
import com.techelevator.exception.ConflictException;
import com.techelevator.exception.DaoException;
import com.techelevator.exception.NotFoundException;
import com.techelevator.model.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Turns every exception into the same JSON shape ({@link ApiError}) so the frontend can show useful messages. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> noRoute(NoResourceFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "No endpoint " + request.getMethod() + " " + request.getRequestURI(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ApiError body = new ApiError(Instant.now(), 400, "Bad Request", "Validation failed",
                request.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Malformed request body. Dates use ISO format, e.g. 2030-01-07T09:30:00", request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> badParameter(Exception e, HttpServletRequest request) {
        String message = e instanceof MethodArgumentTypeMismatchException mismatch
                ? "Invalid value '" + mismatch.getValue() + "' for parameter '" + mismatch.getName() + "'"
                : e.getMessage();
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentials(BadCredentialsException e, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Username or password is incorrect", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> unauthenticated(AuthenticationException e, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden(AccessDeniedException e, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> responseStatus(ResponseStatusException e, HttpServletRequest request) {
        return build(e.getStatusCode(), e.getReason(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, integrityMessage(e), request);
    }

    @ExceptionHandler(DaoException.class)
    public ResponseEntity<ApiError> dao(DaoException e, HttpServletRequest request) {
        if (e.getCause() instanceof DataIntegrityViolationException integrity) {
            return build(HttpStatus.CONFLICT, integrityMessage(integrity), request);
        }
        log.error("Database error on {} {}", request.getMethod(), request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    /** Gives a readable message for the unique constraints in schema.sql. */
    private static String integrityMessage(DataIntegrityViolationException e) {
        String detail = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase();
        if (detail.contains("ux_appointments_doctor_slot")) {
            return "The doctor already has an appointment at that time";
        }
        if (detail.contains("email")) {
            return "That email address is already in use";
        }
        if (detail.contains("insurance_number")) {
            return "That insurance number is already in use";
        }
        if (detail.contains("username")) {
            return "That username is already taken";
        }
        return "The request conflicts with existing data";
    }

    private static ResponseEntity<ApiError> build(HttpStatusCode status, String message, HttpServletRequest request) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String reason = resolved != null ? resolved.getReasonPhrase() : String.valueOf(status.value());
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), reason, message, request.getRequestURI()));
    }
}
