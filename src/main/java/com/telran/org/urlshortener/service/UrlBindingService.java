package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;

import java.net.URI;
import java.util.List;

/**
 * Service interface for managing URL bindings.
 * A URL binding represents a shortened URL associated with a specific user.
 *
 * <p>This interface defines operations for creating, retrieving, resetting,
 * deleting, and resolving URL bindings. Implementations must enforce
 * authorization rules, validate URL formats, and ensure prefix ownership.</p>
 */
public interface UrlBindingService {

    /**
     * Creates a new URL binding for the authenticated user.
     * If a prefix is provided, the user must have an active subscription for it.
     * If a binding with the same UID already exists, it may be reused.
     *
     * @param dto the DTO containing the original URL
     * @param pathPrefix optional prefix associated with a subscription
     * @return the created or reused URL binding as a DTO
     * @throws NullPointerException if dto is null
     * @throws IllegalArgumentException if URL or prefix format is invalid
     * @throws com.telran.org.urlshortener.exception.PathPrefixAvailabilityException
     *         if the prefix does not belong to the user or is not active
     */
    UrlBindingDTO create(UrlBindingCreateDTO dto, String pathPrefix);

    /**
     * Retrieves all URL bindings belonging to the specified user.
     * Regular users may access only their own bindings.
     *
     * @param userId the user ID
     * @return list of URL bindings
     * @throws IllegalArgumentException if userId is not positive
     * @throws org.springframework.security.access.AccessDeniedException
     *         if a regular user attempts to access another user's bindings
     */
    List<UrlBindingDTO> findAllByUserId(long userId);

    /**
     * Retrieves a URL binding by its unique identifier (UID).
     * Regular users may access only their own bindings.
     *
     * @param uId the unique identifier of the binding
     * @return the URL binding DTO
     * @throws IllegalArgumentException if uId format is invalid
     * @throws com.telran.org.urlshortener.exception.IdNotFoundException
     *         if no binding with the given UID exists
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the binding belongs to another user
     */
    UrlBindingDTO find(String uId);

    /**
     * Resets the request counter for a URL binding.
     * Only the owner of the binding may reset it.
     *
     * @param id the binding ID
     * @return the updated URL binding DTO
     * @throws IllegalArgumentException if id is invalid
     * @throws com.telran.org.urlshortener.exception.IdNotFoundException
     *         if the binding does not exist
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the binding belongs to another user
     */
    UrlBindingDTO reset(long id);

    /**
     * Deletes a URL binding.
     * Only the owner of the binding may delete it.
     *
     * @param id the binding ID
     * @throws IllegalArgumentException if id is invalid
     * @throws com.telran.org.urlshortener.exception.IdNotFoundException
     *         if the binding does not exist
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the binding belongs to another user
     */
    void delete(long id);

    /**
     * Resolves a raw request path into a redirect target URL.
     * Increments the request counter for the binding.
     *
     * @param rawPath the incoming request path
     * @return the target URI to redirect to
     * @throws IllegalArgumentException if the path format is invalid or protected
     * @throws com.telran.org.urlshortener.exception.IdNotFoundException
     *         if no binding matches the path
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the redirect target is considered unsafe
     */
    URI resolveRedirectUri(String rawPath);
}