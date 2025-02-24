package com.example.securityservice.service;

import com.example.securityservice.dto.UserRequestDto;
import com.example.securityservice.entity.User;
import com.example.securityservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        userRepository.save(user);
        return "User has been registered";
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
}
