package org.example.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class IOException extends RuntimeException {
    public IOException(String message) {
        super(message);
    }
}
