package org.example.api.exceptions;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Controller
public class CustomErrorController implements ErrorController {
    private static final String PATH = "/error";

    ErrorAttributes errorAttributes;

    @RequestMapping(CustomErrorController.PATH)
    public ResponseEntity<ErrorDto> error(WebRequest webRequest) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(
                webRequest,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.EXCEPTION,
                        ErrorAttributeOptions.Include.MESSAGE)
        );

        return ResponseEntity
                .status((Integer) attributes.get("status"))
                .body(ErrorDto
                        .builder()
                        .error((String) attributes.get("error"))
                        .errorDescription((String) attributes.get("message"))
                        .build()
                );
    }
}