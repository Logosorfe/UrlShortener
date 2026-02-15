package com.telran.org.urlshortener.dto;

import com.telran.org.urlshortener.model.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a user in API responses.
 * Contains public, non-sensitive user information such as ID, email, and role.
 *
 * <p>This DTO is used to expose user data to clients without revealing
 * internal fields such as passwords or security details.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    /**
     * Unique identifier of the user.
     */
    private Long id;
    /**
     * Email address of the user. * Always stored and returned in lowercase.
     */
    private String email;
    /**
     * Role assigned to the user (e.g., USER or ADMIN).
     */
    private RoleType role;
}