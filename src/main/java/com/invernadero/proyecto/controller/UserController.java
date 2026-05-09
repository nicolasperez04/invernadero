package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Dto.Request.UserRequest;
import com.invernadero.proyecto.Dto.response.UserResponse;
import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Entity.enums.Role;
import com.invernadero.proyecto.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details. Only accessible by ADMIN role.")
    @ApiResponse(responseCode = "200", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID. Accessible by ADMIN and OPERATOR roles.")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @Operation(summary = "Get all users", description = "Retrieves a list of all users. Only accessible by ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @Operation(summary = "Update user", description = "Updates an existing user with the provided details. Only accessible by ADMIN role.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "Delete user", description = "Deletes a user by their ID. Only accessible by ADMIN role.")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
