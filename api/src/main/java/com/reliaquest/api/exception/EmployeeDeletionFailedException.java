package com.reliaquest.api.exception;

import lombok.Getter;

@Getter
public class EmployeeDeletionFailedException extends RuntimeException {
    private final String employeeId;

    public EmployeeDeletionFailedException(String employeeId) {
        super("Failed to delete employee with ID '" + employeeId + "'");
        this.employeeId = employeeId;
    }
}
