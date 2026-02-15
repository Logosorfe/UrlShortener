package com.telran.org.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object used for creating a new subscription.
 * Contains the required path prefix that the user wants to reserve.
 *
 * <p>The prefix must meet validation rules and must not be already
 * owned by another active subscription.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreateDTO {
    /**
     * Path prefix requested by the user.
     * Must contain only letters, digits, hyphens, and underscores.
     */
    @NotBlank
    @Size(min = 3, max = 50, message = "pathPrefix length must be between 3 and 50")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "pathPrefix may contain only letters, digits, hyphen and underscore"
    )
    private String pathPrefix;
}