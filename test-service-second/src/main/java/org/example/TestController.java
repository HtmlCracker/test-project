package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/second-test")
public class TestController {
    private final RestTemplate restTemplate;

    public TestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String getTest() {
        String answer = this.restTemplate.getForObject("http://test-service-first/api/first-test", String.class);
        return answer + "dsfdsffsdfsdf";
    }
}
