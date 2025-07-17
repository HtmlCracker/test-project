package com.example.chatservice.client;

import com.example.chatservice.dto.MemberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "profile-service", url = "http://profile-service:45291")
public interface ProfileClient {
    @GetMapping("api/public/accounts/get/{profileId}")
    MemberDto getProfileDto(@PathVariable UUID userId);
}
