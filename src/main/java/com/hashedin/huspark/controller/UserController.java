package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.PaginatedResponse;
import com.hashedin.huspark.dto.UserResponse;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
        
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("This is an admin-only endpoint!");
    }

    @GetMapping("/member-or-above")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<String> memberOrAbove() {
        return ResponseEntity.ok("This endpoint is accessible to all authenticated users!");
    }

    // ADMIN endpoints for user management
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PaginatedResponse<UserResponse> users = userService.getAllUsersPaginated(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponse>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PaginatedResponse<UserResponse> users = userService.searchUsersPaginated(q, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponse>> getUsersByRole(
            @PathVariable Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PaginatedResponse<UserResponse> users = userService.getUsersByRolePaginated(role, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponse>> filterUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PaginatedResponse<UserResponse> users = userService.getUsersWithFilters(name, email, role, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(users);
    }
}
