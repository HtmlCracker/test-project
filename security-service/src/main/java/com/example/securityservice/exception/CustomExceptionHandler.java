package com.example.securityservice.exception;

import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> badRequestException(BadRequestException ex) {
        ErrorResponse response = new ErrorResponse("Bad Request" + ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFound(EntityNotFoundException e){
        ErrorResponse response = new ErrorResponse("Entity not found", LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(404));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalidArgument(MethodArgumentNotValidException e){
        BindingResult result = e.getBindingResult();
        HashMap<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        ErrorResponse response = new ErrorResponse(errors.toString(), LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(400));
    }
}
