package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.AuthResponse;
import com.hashedin.huspark.dto.PaginatedResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

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

        // Log the user registration
        auditService.logUserRegistration(savedUser);

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



    public PaginatedResponse<UserResponse> getAllUsersPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);
        
        return convertToPaginatedResponse(userPage);
    }

    public PaginatedResponse<UserResponse> getUsersByRolePaginated(Role role, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findByRole(role, pageable);
        
        return convertToPaginatedResponse(userPage);
    }

    public PaginatedResponse<UserResponse> searchUsersPaginated(String searchTerm, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.searchUsers(searchTerm, pageable);
        
        return convertToPaginatedResponse(userPage);
    }

    public PaginatedResponse<UserResponse> getUsersWithFilters(String name, String email, Role role, 
                                                             int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findUsersWithFilters(name, email, role, pageable);
        
        return convertToPaginatedResponse(userPage);
    }

    private PaginatedResponse<UserResponse> convertToPaginatedResponse(Page<User> userPage) {
        List<UserResponse> content = userPage.getContent().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        
        return new PaginatedResponse<>(
            content,
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.hasNext(),
            userPage.hasPrevious(),
            userPage.isFirst(),
            userPage.isLast()
        );
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
