package com.reliaquest.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EmployeeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(EmployeeDeletionFailedException.class)
    public ResponseEntity<String> handleEmployeeDeletionFailed(EmployeeDeletionFailedException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(InvalidEmployeeInputException.class)
    public ResponseEntity<String> handleInvalidEmployeeInput(InvalidEmployeeInputException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An unexpected error occurred: " + e.getMessage());
    }
}
