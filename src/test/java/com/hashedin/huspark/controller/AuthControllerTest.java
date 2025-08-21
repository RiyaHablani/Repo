package com.hashedin.huspark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huspark.dto.AuthResponse;
import com.hashedin.huspark.dto.UserLoginRequest;
import com.hashedin.huspark.dto.UserRegistrationRequest;
import com.hashedin.huspark.dto.UserResponse;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.exception.UserAlreadyExistsException;
import com.hashedin.huspark.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;
    private AuthResponse authResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setName("John Doe");
        registrationRequest.setEmail("john.doe@example.com");
        registrationRequest.setPassword("password123");

        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("John Doe");
        userResponse.setEmail("john.doe@example.com");
        userResponse.setRole(Role.MEMBER);

        authResponse = new AuthResponse();
        authResponse.setToken("jwt-token");
        authResponse.setUser(userResponse);
    }

    @Test
    void testRegister_WhenValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.user.name").value("John Doe"))
                .andExpect(jsonPath("$.user.role").value("MEMBER"));
    }

    @Test
    void testRegister_WhenInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid request with missing required fields
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
        invalidRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_WhenUserAlreadyExists_ShouldReturnConflict() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists with email: john.doe@example.com"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("User already exists with email: john.doe@example.com"));
    }

    @Test
    void testLogin_WhenValidCredentials_ShouldReturnOk() throws Exception {
        // Given
        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"));
    }
}
