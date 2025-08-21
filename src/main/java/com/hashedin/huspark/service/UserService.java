package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.AuthResponse;
import com.hashedin.huspark.dto.UserLoginRequest;
import com.hashedin.huspark.dto.UserRegistrationRequest;
import com.hashedin.huspark.dto.UserResponse;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.exception.UserAlreadyExistsException;
import com.hashedin.huspark.exception.UserNotFoundException;
import com.hashedin.huspark.repository.UserRepository;
import com.hashedin.huspark.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse registerUser(UserRegistrationRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.MEMBER); // Default role

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        // Convert to response DTO
        UserResponse userResponse = convertToUserResponse(savedUser);
        return new AuthResponse(token, userResponse);
    }

    public AuthResponse loginUser(UserLoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Convert to response DTO
        UserResponse userResponse = convertToUserResponse(user);
        return new AuthResponse(token, userResponse);
    }



    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
