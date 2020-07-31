package com.mintyi.fablix.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class RoleValidationException extends RuntimeException {

    public RoleValidationException(String message) {
        super(message);
    }
}
