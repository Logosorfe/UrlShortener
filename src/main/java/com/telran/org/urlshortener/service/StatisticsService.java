package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingDTO;

import java.util.List;

/**
 * Service interface for retrieving statistical information about URL usage.
 * Provides operations for counting redirect requests per user, per URL binding,
 * and retrieving the most frequently accessed shortened URLs.
 *
 * <p>Implementations must enforce authorization rules so that regular users
 * may access only their own statistics, while administrators may access
 * global statistics.</p>
 */
public interface StatisticsService {

    /**
     * Returns the total number of redirect requests for all URL bindings
     * belonging to the specified user.
     * Regular users may access only their own statistics.
     *
     * @param userId the user ID
     * @return the total number of redirect requests for the user
     * @throws IllegalArgumentException if userId is not positive
     * @throws org.springframework.security.access.AccessDeniedException
     *         if a regular user attempts to access another user's statistics
     */
    Long requestsNumberByUser(long userId);

    /**
     * Returns the number of redirect requests for a specific URL binding.
     * Regular users may access only their own bindings.
     *
     * @param urlBindingId the ID of the URL binding
     * @return the number of redirect requests for the binding
     * @throws IllegalArgumentException if urlBindingId is not positive
     * @throws com.telran.org.urlshortener.exception.IdNotFoundException
     *         if the binding does not exist
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the binding belongs to another user
     */
    long requestsNumberByUrlBinding(long urlBindingId);

    /**
     * Returns the top 10 most frequently accessed URL bindings in the system.
     * Accessible only to administrators.
     *
     * @return a list of the top 10 URL bindings sorted by request count
     */
    List<UrlBindingDTO> topTenRequests();
}