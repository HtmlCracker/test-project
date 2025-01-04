package org.example.api.client;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackages="com.howtodoinjava.feign.client")
@Configuration
public class FeignConfig {

}
