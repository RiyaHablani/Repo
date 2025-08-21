package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.AuthResponse;
import com.hashedin.huspark.dto.UserLoginRequest;
import com.hashedin.huspark.dto.UserRegistrationRequest;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.exception.UserAlreadyExistsException;
import com.hashedin.huspark.exception.UserNotFoundException;
import com.hashedin.huspark.repository.UserRepository;
import com.hashedin.huspark.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.MEMBER);

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setName("John Doe");
        registrationRequest.setEmail("john.doe@example.com");
        registrationRequest.setPassword("password123");

        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegisterUser_WhenValidRequest_ShouldReturnAuthResponse() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        // When
        AuthResponse response = userService.registerUser(registrationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getUser().getName()).isEqualTo("John Doe");
        assertThat(response.getUser().getRole()).isEqualTo(Role.MEMBER);

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("john.doe@example.com");
    }

    @Test
    void testRegisterUser_WhenUserAlreadyExists_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(registrationRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User already exists with email: john.doe@example.com");

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
    }



    @Test
    void testLoginUser_WhenValidCredentials_ShouldReturnAuthResponse() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        // When
        AuthResponse response = userService.loginUser(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil).generateToken("john.doe@example.com");
    }

    @Test
    void testLoginUser_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: john.doe@example.com");

        verify(userRepository).findByEmail("john.doe@example.com");
    }


}
