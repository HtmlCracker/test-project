package org.example.api.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class EmailController {
    @Autowired
    TemplateEngine templateEngine;

    public static final String GET_CHANGE_PASSWORD_PAGE = "api/public/email/change-password/{token}";

    @GetMapping(GET_CHANGE_PASSWORD_PAGE)
    public String getChangePasswordPage(@PathVariable String token) {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("token", token);

        Context context = new Context();
        context.setVariables(variables);

        return templateEngine.process("changePassword", context);
    }
}
