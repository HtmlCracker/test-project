package com.example.securityservice.service;

import com.example.securityservice.dto.ResetPasswordRequestDto;
import com.example.securityservice.dto.UserRequestDto;
import com.example.securityservice.entity.PasswordResetEntity;
import com.example.securityservice.entity.User;
import com.example.securityservice.exception.BadRequestException;
import com.example.securityservice.repository.PasswordResetRepository;
import com.example.securityservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final EmailVerifyService emailVerifyService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetRepository passwordResetRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       PasswordResetRepository passwordResetRepository,
                       @Lazy EmailVerifyService emailVerifyService) {
        this.emailVerifyService = emailVerifyService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetRepository = passwordResetRepository;
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Transactional
    public String save(UserRequestDto userRequestDto){
        if (userRepository.findByEmail(userRequestDto.getEmail()).isPresent())
            return "This email has already registered";
        User user = new User(
                userRequestDto.getEmail(),
                passwordEncoder.encode(userRequestDto.getPassword()),
                LocalDateTime.now(),
                false);
        //TODO отправка email с кодом
        emailVerifyService.sendVerifyToken(userRequestDto.getEmail());
        userRepository.save(user);
        return "User has been registered";
    }

    @Transactional
    public void updateEnabled(String email, boolean isEnabled) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setEnabled(isEnabled);
        userRepository.save(user);
    }

    @Transactional
    public String update(String email, UserRequestDto userUpd) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(userUpd.getPassword()));
        user.setEmail(userUpd.getEmail());

        if (user.getEmail().equals(userUpd.getEmail())){
            userRepository.save(user);
            return "User has been updated";
        }

        user.setEmail(userUpd.getEmail());
        user.setEnabled(false);
        userRepository.save(user);
        //TODO отправка email с кодом
        return "User has been updated. Please, confirm your email with new activation code we sent you";
    }

    @Transactional
    public String delete(User user){
        userRepository.delete(user);
        return "User has been deleted";
    }

    @Transactional
    public void forgotPassword(String email) {
        UUID token = UUID.randomUUID();

        Optional<User> user = findByEmail(email);

        if (user.isEmpty()) {
            throw new BadRequestException(
                    String.format("Account with email %s is not exists.", email)
            );
        } if (!user.get().isEnabled()) {
            throw new BadRequestException(
                    String.format("Account with email %s is not enabled.", email)
            );
        }

        PasswordResetEntity passwordResetEntity = findOrCreatePasswordResetEntity(email, token);

        passwordResetRepository.save(passwordResetEntity);
        emailVerifyService.sendForgotPassword(email, token);
    }

    private PasswordResetEntity findOrCreatePasswordResetEntity(String email, UUID token) {
        if (passwordResetRepository.findByEmail(email).isPresent()) {
            PasswordResetEntity passwordResetEntity =
                    passwordResetRepository.findByEmail(email).get();

            passwordResetEntity.setToken(token);
            return passwordResetEntity;
        }
        return PasswordResetEntity.builder()
                .email(email)
                .token(token)
                .build();
    }

    @Transactional
    public String resetPassword(ResetPasswordRequestDto dto) {
        String firstPassword = dto.getFirstPassword();
        String secondPassword = dto.getSecondPassword();
        UUID token = UUID.fromString(dto.getToken());

        PasswordResetEntity passwordResetEntity = passwordResetRepository.findByToken(token)
                .orElseThrow(
                        () -> new BadRequestException(String.format("Entity with token %s is not exists", token))
                );

        String email = passwordResetEntity.getEmail();

        validatePasswordsMatch(firstPassword, secondPassword);

        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(firstPassword));

        userRepository.save(user);
        passwordResetRepository.delete(passwordResetEntity);
        System.out.println("OOOOOOK");
        return "OK";
    }

    private void validatePasswordsMatch(String firstPassword, String secondPassword) {
        if (!firstPassword.equals(secondPassword)) {
            throw new BadRequestException("Passwords do not match");
        }
    }
}
