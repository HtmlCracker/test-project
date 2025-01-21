package com.example.securityservice.service;

import com.example.securityservice.entity.RefreshToken;
import com.example.securityservice.entity.User;
import com.example.securityservice.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public String createRefreshToken(User user){
        RefreshToken refreshToken = new RefreshToken(UUID.randomUUID().toString(),
                LocalDateTime.now().plusMonths(6),
                user);
        Optional<RefreshToken> oldToken = refreshTokenRepository.findByUser(user);
        if (oldToken.isPresent())
            refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    public String refreshJwt(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(EntityNotFoundException::new);
        if (refreshToken.getExpiryDate().isAfter(LocalDateTime.now()))
            return "Refresh token has expired";
        return jwtService.generateToken(refreshToken.getUser());
    }
}
