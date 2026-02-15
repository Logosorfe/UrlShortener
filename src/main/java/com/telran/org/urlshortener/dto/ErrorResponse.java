package com.telran.org.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API error response structure.
 * Returned by the global exception handler when an error occurs.
 *
 * <p>Contains timestamp, HTTP status, error type, message, and request path.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    /**
     * Timestamp when the error occurred.
     */
    private Instant timestamp;
    /**
     * HTTP status code of the error.
     */
    private int status;
    /**
     * Short description of the error type.
     */
    private String error;
    /**
     * Detailed error message intended for the client.
     */
    private String message;
    /**
     * The request path that triggered the error.
     */
    private String path;
}