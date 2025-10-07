package com.reliaquest.api.exception;

import lombok.Getter;

@Getter
public class InvalidEmployeeInputException extends RuntimeException {

    public InvalidEmployeeInputException(String message) {
        super(message);
    }
}
