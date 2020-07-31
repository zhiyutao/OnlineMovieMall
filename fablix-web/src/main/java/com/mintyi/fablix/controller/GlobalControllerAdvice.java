package com.mintyi.fablix.controller;

import com.mintyi.fablix.controller.exception.RoleValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(RoleValidationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> roleExceptionHandler(RoleValidationException e) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "403 Forbidden");
        map.put("message", e.getMessage());
        return map;
    }
}
