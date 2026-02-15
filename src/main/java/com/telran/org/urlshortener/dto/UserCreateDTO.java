package com.telran.org.urlshortener.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object used for creating a new user.
 * Contains the required registration fields such as email and password.
 *
 * <p>This DTO is used in user registration requests and is validated
 * using Jakarta Bean Validation annotations.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    /**
     * Email address of the new user.
     * Must be a valid email format and cannot be blank.
     */
    @Email
    @NotBlank
    private String email;

    /**
     * Raw password provided during registration.
     * Must meet minimum length requirements.
     * The password is encoded before being stored.
     */
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}