package com.reliaquest.api.exception;

import lombok.Getter;

@Getter
public class EmployeeNotFoundException extends RuntimeException {
    private final String employeeId;

    public EmployeeNotFoundException(String employeeId) {
        super("Employee with ID '" + employeeId + "' not found");
        this.employeeId = employeeId;
    }
}
