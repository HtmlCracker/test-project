package com.example.chatservice.client;

import com.example.chatservice.dto.ProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "profile-service", url = "http://profile-service:45291")
public interface ProfileClient {
    @GetMapping("api/public/accounts/get/{profileId}")
    ProfileDto getProfileDto(@PathVariable UUID userId);

    @GetMapping("api/public/accounts/")
    List<ProfileDto> getProfilesByUserIds(@RequestParam List<UUID> userIds); //todo реализовать
}
