package com.glassfinder.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==========================================
    // DUPLICATE BOX
    // ==========================================

    @ExceptionHandler(DuplicateBoxException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBox(
            DuplicateBoxException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                "Duplicate Box",
                exception.getMessage()
        );
    }


    // ==========================================
    // BOX NOT FOUND
    // ==========================================

    @ExceptionHandler(BoxNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBoxNotFound(
            BoxNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                "Box Not Found",
                exception.getMessage()
        );
    }


    // ==========================================
    // INSUFFICIENT STOCK
    // ==========================================

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                "Insufficient Stock",
                exception.getMessage()
        );
    }


    // ==========================================
    // VALIDATION ERROR
    // ==========================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .orElse("Invalid request");

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                message
        );
    }


    // ==========================================
    // GENERAL VALIDATION
    // ==========================================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                exception.getMessage()
        );
    }


    // ==========================================
    // RESPONSE BUILDER
    // ==========================================

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String error,
            String message
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        status.value(),
                        error,
                        message,
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }


    // ==========================================
    // ERROR DTO
    // ==========================================

    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {
    }
}