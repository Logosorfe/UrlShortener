package com.telran.org.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object used for creating a new URL binding.
 * Contains the original URL that should be shortened.
 *
 * <p>The URL is validated and normalized before being stored.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlBindingCreateDTO {
    /**
     * The original URL provided by the user.
     * Must be a valid HTTP or HTTPS URL.
     */
    @NotBlank
    @Size(max = 2048)
    private String originalUrl;
}