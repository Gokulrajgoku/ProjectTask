package com.gokulraj.localstorage.service;

import com.gokulraj.localstorage.model.User;
import com.gokulraj.localstorage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("User already exists");
        }

        String encryptedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encryptedPassword, role);
        return userRepository.save(user); // Save user to MongoDB
    }
}
