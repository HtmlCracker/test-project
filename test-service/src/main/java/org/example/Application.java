package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/first-test")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
    }

    @GetMapping
    public String getTest() {
        return "Test is OK";
    }
}