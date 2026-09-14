package com.tanesco.faultmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {

        Map<String, Object> response =
                createResponse(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException exception
    ) {

        Map<String, Object> response =
                createResponse(
                        HttpStatus.CONFLICT,
                        "Conflict",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException exception
    ) {

        Map<String, Object> response =
                createResponse(
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException exception
    ) {

        Map<String, Object> response =
                createResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Invalid username or password."
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        for (
                FieldError error :
                exception.getBindingResult()
                        .getFieldErrors()
        ) {

            validationErrors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                LocalDateTime.now()
        );

        response.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        response.put(
                "error",
                "Validation Failed"
        );

        response.put(
                "message",
                "One or more fields are invalid."
        );

        response.put(
                "validationErrors",
                validationErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception exception
    ) {

        System.err.println(
                "========================================"
        );

        System.err.println(
                "UNEXPECTED APPLICATION ERROR"
        );

        System.err.println(
                "Exception type: "
                        + exception.getClass().getName()
        );

        System.err.println(
                "Exception message: "
                        + exception.getMessage()
        );

        System.err.println(
                "========================================"
        );

        exception.printStackTrace();

        Map<String, Object> response =
                createResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "An unexpected error occurred."
                );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(response);
    }

    private Map<String, Object> createResponse(
            HttpStatus status,
            String error,
            String message
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                LocalDateTime.now()
        );

        response.put(
                "status",
                status.value()
        );

        response.put(
                "error",
                error
        );

        response.put(
                "message",
                message
        );

        return response;
    }
}