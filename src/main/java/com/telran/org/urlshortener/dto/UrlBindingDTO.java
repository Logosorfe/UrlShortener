package com.telran.org.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a shortened URL binding.
 * Contains the original URL, the generated short URL, and usage statistics.
 *
 * <p>This DTO is returned to clients when creating or retrieving URL bindings.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlBindingDTO {
    /**
     * Unique identifier of the URL binding.
     */
    private Long id;
    /**
     * The original full URL provided by the user.
     */
    private String originalUrl;
    /**
     * The generated short URL that redirects to the original URL.
     */
    private String shortUrl;
    /**
     * Number of times the short URL has been accessed.
     */
    private Long count;
}