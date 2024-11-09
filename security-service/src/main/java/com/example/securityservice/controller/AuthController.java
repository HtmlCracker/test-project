package com.example.securityservice.controller;

import com.example.securityservice.dto.RequestDto;
import com.example.securityservice.entity.User;
import com.example.securityservice.service.JwtService;
import com.example.securityservice.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody RequestDto requestDto){
        return userService.save(requestDto);
    }

    //TODO /activate

    @PatchMapping("/update")
    public String update(@Valid @RequestBody RequestDto userUpd, @RequestHeader("Authorization") String authHeader){
        return userService.update(jwtService.extractEmail(authHeader.substring(7)), userUpd);
    }

    @DeleteMapping("/delete")
    public String delete(@RequestHeader("Authorization") String authHeader){
        return userService.delete(userService.findByEmail(jwtService.extractEmail(authHeader.substring(7))).orElseThrow(()->new EntityNotFoundException("User not found")));
    }

    @PostMapping("/token")
    public String getToken(@Valid @RequestBody RequestDto requestDto){
        User user = userService.findByEmail(requestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        if (!user.isEnabled())
            return "Your account is not activated";

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                requestDto.getEmail(),
                requestDto.getPassword()));
        if (authentication.isAuthenticated())
            return jwtService.generateToken(user);

        return "Invalid username or password";
    }

    @GetMapping("/validate")
    public String isTokenValid(@RequestParam("token") String token){
        try {
            User user = userService.findByEmail(jwtService.extractEmail(token)).orElseThrow(EntityNotFoundException::new);
            if (jwtService.isTokenValid(token,user))
                return "Token is valid";
        } catch (ExpiredJwtException e){}
        return "Invalid token";
    }
}
