package com.example.securityservice.controller;

import com.example.securityservice.dto.ResetPasswordRequestDto;
import com.example.securityservice.dto.UserRequestDto;
import com.example.securityservice.dto.kafka.AccountDeletedDto;
import com.example.securityservice.entity.User;
import com.example.securityservice.service.*;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerifyService emailVerifyService;
    private final AuthenticationManager authenticationManager;
    private final ProducerService producerService;

    @Autowired
    public AuthController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager, EmailVerifyService emailVerifyService, ProducerService producerService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailVerifyService = emailVerifyService;
        this.authenticationManager = authenticationManager;
        this.producerService = producerService;
    }

    @PostMapping("/forgot-password/{email}")
    public void forgotPassword(@PathVariable String email) {
        userService.forgotPassword(email);
    }

    @PostMapping("/reset-password/")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequestDto dto) {
        System.out.println(dto.getFirstPassword());
        return userService.resetPassword(dto);
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody UserRequestDto userRequestDto) {
        return userService.save(userRequestDto);
    }

    @PostMapping("/email/activate")
    public void activateEmail(@RequestParam("token") String token) {
        emailVerifyService.activateEmail(token);
    }

    @PatchMapping("/update")
    public String update(@Valid @RequestBody UserRequestDto userUpd,
                         @RequestHeader("Authorization") String authHeader) {
        return userService.update(jwtService.extractEmail(authHeader.substring(7)), userUpd);
    }

    @DeleteMapping("/delete")
    public String delete(@RequestHeader("Authorization") String authHeader) {
        UUID id = UUID.fromString(jwtService.extractUserId(authHeader.substring(7)));

        AccountDeletedDto accountDeletedDto = AccountDeletedDto.builder()
                .accountId(id)
                .time(Instant.now().toString())
                .build();

        producerService.sendDeletedAccountMessage(accountDeletedDto);
        userService.deleteUserById(id);
        String response = String.format("User with id: %s was deleted.", id);
        return response;
    }

    @PostMapping("/token")
    public String getToken(@Valid @RequestBody UserRequestDto userRequestDto) {
        User user = userService.findByEmail(userRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        if (!user.isEnabled())
            return "Your account is not activated";

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userRequestDto.getEmail(),
                userRequestDto.getPassword()));
        if (authentication.isAuthenticated())
            return "Your JWT token: " + jwtService.generateToken(user) + "\n" +
                    "Your Refresh token: " + refreshTokenService.createRefreshToken(user);

        return "Invalid username or password";
    }

    @PostMapping("/refresh")
    public String refresh(@RequestBody String token) {
        return refreshTokenService.refreshJwt(token);
    }

    @GetMapping("/validate")
    public String isTokenValid(@RequestParam("token") String token) {
        try {
            User user = userService.findByEmail(jwtService.extractEmail(token)).orElseThrow(EntityNotFoundException::new);
            if (jwtService.isTokenValid(token, user))
                return "Token is valid";
        } catch (ExpiredJwtException e) {}
        return "Invalid token";
    }
}
