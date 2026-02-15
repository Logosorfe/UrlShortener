package com.telran.org.urlshortener.utility;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Utility service responsible for validating and normalizing URLs and UID paths.
 * Provides methods for checking URL format, ensuring safe redirect targets,
 * and validating the structure of shortened URL identifiers.
 *
 * <p>This component is used by URL binding services to ensure that all URLs
 * and UID paths meet security and formatting requirements before processing.</p>
 */
@Component
public class UrlValidationService {
    /**
     * Normalizes the original URL by trimming whitespace.
     *
     * @param url the raw URL provided by the user
     * @return the normalized URL
     * @throws NullPointerException if url is null
     */
    public String normalizeUrl(String url) {
        return Objects.requireNonNull(url, "originalUrl must not be null").trim();
    }

    /**
     * Validates that the URL is syntactically correct and uses HTTP or HTTPS.
     *
     * @param url the URL to validate
     * @throws IllegalArgumentException if the URL is invalid or uses an unsupported scheme
     */
    public void validateUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Invalid URL format");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL format");
        }
    }
    /**
     * Normalizes a UID by trimming whitespace and ensuring it starts with a slash.
     *
     * @param uId the raw UID
     * @return the normalized UID
     * @throws NullPointerException if uId is null
     * @throws IllegalArgumentException if uId is blank
     */
    public String normalizeUId(String uId) {
        Objects.requireNonNull(uId, "uId must not be null");
        String trimmed = uId.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("uId must not be blank");
        if (!trimmed.startsWith("/")) trimmed = "/" + trimmed;
        return trimmed;
    }
    /**
     * Checks whether the given UID matches the expected format for shortened URLs.
     *
     * @param uId the UID to validate
     * @return true if the UID is valid, false otherwise
     */
    public boolean isValidUId(String uId) {
        if (uId == null) return false;
        return uId.matches("^/[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+)?$");
    }
    /**
     * Validates whether a redirect target URL is safe.
     * Rejects localhost, private network ranges, and URLs without HTTP/HTTPS schemes.
     *
     * @param url the URL to validate
     * @return true if the URL is safe for redirecting, false otherwise
     */
    public boolean isValidRedirectUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return false;
            }
            String host = uri.getHost();
            if (host == null) return false;
            List<String> blockedHosts = List.of("localhost", "127.0.0.1");
            if (blockedHosts.contains(host)) return false;
            List<String> internalPrefixes = List.of("10.", "192.168.", "172.16.");
            return internalPrefixes.stream().noneMatch(host::startsWith);
        } catch (Exception e) {
            return false;
        }
    }
}