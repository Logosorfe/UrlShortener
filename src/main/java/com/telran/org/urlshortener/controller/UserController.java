package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.security.AuthenticationService;
import com.telran.org.urlshortener.security.JwtAuthenticationResponse;
import com.telran.org.urlshortener.security.SignInRequest;
import com.telran.org.urlshortener.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    private final AuthenticationService authentication;

    @PostMapping
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(service.findUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable @Positive long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<UserDTO> setUserRole(@PathVariable @Positive long id,
                                               @RequestParam @NotNull RoleType newRole) {
        return ResponseEntity.ok(service.update(id, newRole));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeUserById(@PathVariable @Positive long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam @Email @NotBlank String email) {
        return ResponseEntity.ok(service.findByEmail(email));
    }

    @PostMapping("/login")
    public JwtAuthenticationResponse login(@RequestBody SignInRequest request) {
        return authentication.authenticate(request);
    }
}
