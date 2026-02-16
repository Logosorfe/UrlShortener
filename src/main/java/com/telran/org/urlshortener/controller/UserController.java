package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.security.AuthenticationService;
import com.telran.org.urlshortener.security.JwtAuthenticationResponse;
import com.telran.org.urlshortener.security.SignInRequest;
import com.telran.org.urlshortener.service.UserService;
import com.telran.org.urlshortener.utility.MaskingUtil;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users",
        description = "Operations for managing users, authentication and roles",
        externalDocs = @ExternalDocumentation(description = "Full API documentation", url = "https://example.com/docs"))
@RestController
@RequestMapping("/users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    private final AuthenticationService authentication;

    private final MaskingUtil masking;

    @Operation(summary = "Register a new user",
            description = "Creates a new user account with email and password",
            operationId = "registerUser",
            tags = {"Users"},
            deprecated = false,
            externalDocs = @ExternalDocumentation(description = "Registration rules",
                    url = "https://example.com/docs/registration"), security = {})
    @ApiResponses({@ApiResponse(responseCode = "201",
            description = "User successfully created",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")})
    @PostMapping
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserCreateDTO dto) {
        log.debug("registerUser email={}", masking.maskEmail(dto.getEmail()));
        UserDTO created = service.create(dto);
        log.info("User created id={}, email={}", created.getId(), masking.maskEmail(created.getEmail()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get all users",
            description = "Returns a list of all users (ADMIN only)",
            operationId = "getAllUsers",
            tags = {"Users"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "List of users returned",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.debug("getAllUsers called");
        List<UserDTO> users = service.findAll();
        log.info("getAllUsers returned count={}", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID",
            description = "Returns user details by ID (ADMIN only)",
            operationId = "getUserById",
            tags = {"Users"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable @Positive long id) {
        log.debug("getUserById id={}", id);
        UserDTO dto = service.findById(id);
        log.info("getUserById succeeded id={}, email={}", id, masking.maskEmail(dto.getEmail()));
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Update user role",
            description = "Changes the role of a user (ADMIN only)",
            operationId = "setUserRole",
            tags = {"Users"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Role updated successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid role or ID"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDTO> setUserRole(@PathVariable @Positive long id,
                                               @RequestParam @NotNull RoleType newRole) {
        log.debug("setUserRole id={}, newRole={}", id, newRole);
        UserDTO updated = service.update(id, newRole);
        log.info("setUserRole succeeded id={}, newRole={}", id, newRole);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete user",
            description = "Deletes a user. Regular users may delete only themselves; ADMIN may delete any user.",
            operationId = "deleteUser",
            tags = {"Users"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeUserById(@PathVariable @Positive long id) {
        log.debug("removeUserById id={}", id);
        service.delete(id);
        log.info("removeUserById succeeded id={}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user by email",
            description = "Returns user details by email. Regular users may access only their own data.",
            operationId = "getUserByEmail",
            tags = {"Users"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    @GetMapping("/by-email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam @Email @NotBlank String email) {
        log.debug("getUserByEmail email={}", masking.maskEmail(email));
        UserDTO dto = service.findByEmail(email);
        log.info("getUserByEmail succeeded id={}, email={}", dto.getId(), masking.maskEmail(dto.getEmail()));
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "User login",
            description = "Authenticates user and returns JWT token",
            operationId = "loginUser", tags = {"Users"}, security = {})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")})
    @PostMapping("/login")
    public JwtAuthenticationResponse login(@Valid @RequestBody SignInRequest requestBody) {
        log.debug("login attempt login={}", masking.maskLogin(requestBody.getLogin()));
        JwtAuthenticationResponse resp = authentication.authenticate(requestBody);
        log.info("login succeeded login={}", masking.maskLogin(requestBody.getLogin()));
        return resp;
    }
}