package com.example.securityservice.controller;

import com.example.securityservice.dto.UserRequestDto;
import com.example.securityservice.entity.User;
import com.example.securityservice.service.JwtService;
import com.example.securityservice.service.RefreshTokenService;
import com.example.securityservice.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody UserRequestDto userRequestDto){
        return userService.save(userRequestDto);
    }

    //TODO /activate

    @PatchMapping("/update")
    public String update(@Valid @RequestBody UserRequestDto userUpd, @RequestHeader("Authorization") String authHeader){
        return userService.update(jwtService.extractEmail(authHeader.substring(7)), userUpd);
    }

    @DeleteMapping("/delete")
    public String delete(@RequestHeader("Authorization") String authHeader){
        return userService.delete(userService.findByEmail(jwtService.extractEmail(authHeader.substring(7))).orElseThrow(()->new EntityNotFoundException("User not found")));
    }

    @PostMapping("/token")
    public String getToken(@Valid @RequestBody UserRequestDto userRequestDto){
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
    public String refresh(@RequestBody String token){
        return refreshTokenService.refreshJwt(token);
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
