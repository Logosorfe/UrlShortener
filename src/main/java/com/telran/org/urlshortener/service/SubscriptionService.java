package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.entity.Subscription;

import java.util.List;

/**
 * Service interface for managing user subscriptions.
 * A subscription grants a user the right to use a specific path prefix
 * when creating shortened URLs. Subscriptions may expire and require renewal.
 *
 * <p>This interface defines operations for creating, retrieving, deleting,
 * and paying for subscriptions. Implementations must enforce ownership rules,
 * prefix uniqueness, and expiration logic.</p>
 */
public interface SubscriptionService {

    /**
     * Creates a new subscription for the authenticated user.
     * If a subscription with the same prefix already exists, it may be reused
     * or renewed depending on its expiration state and ownership.
     *
     * @param dto the subscription creation request containing the path prefix
     * @return the created or reused subscription as a DTO
     * @throws NullPointerException if dto is null
     * @throws IllegalArgumentException if the prefix format is invalid
     * @throws com.telran.org.urlshortener.exception.PathPrefixAvailabilityException
     *         if the prefix is currently owned by another active subscription
     */
    SubscriptionDTO create(SubscriptionCreateDTO dto);

    /**
     * Retrieves all subscriptions belonging to the specified user.
     * Regular users may access only their own subscriptions.
     *
     * @param userId the user ID
     * @return list of subscriptions
     * @throws IllegalArgumentException if userId is not positive
     * @throws org.springframework.security.access.AccessDeniedException
     *         if a regular user attempts to access another user's subscriptions
     */
    List<SubscriptionDTO> findAllByUserId(long userId);

    /**
     * Retrieves a subscription by its unique identifier.
     * Regular users may access only their own subscriptions.
     *
     * @param id the subscription ID
     * @return the subscription DTO
     * @throws IllegalArgumentException if id is invalid
     * @throws com.telran.org.urlshortener.exception.IdNotFoundException
     *         if no subscription with the given ID exists
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the subscription belongs to another user
     */
    SubscriptionDTO findById(long id);

    /**
     * Deletes a subscription.
     * Only the owner of the subscription may delete it.
     *
     * @param id the subscription ID
     * @throws IllegalArgumentException if id is invalid
     * @throws com.telran.org.urlshortener.exception.IdNotFoundException
     *         if the subscription does not exist
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the subscription belongs to another user
     */
    void delete(long id);

    /**
     * Initiates the payment process for a subscription.
     * Payment is performed not asynchronously at the moment, but by ADMIN. If successful, the subscription
     * expiration date is extended; otherwise, it remains unpaid.
     *
     * @param id the subscription ID
     * @throws IllegalArgumentException if id is invalid
     * @throws com.telran.org.urlshortener.exception.IdNotFoundException
     *         if the subscription does not exist
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the subscription belongs to another user
     */
    void makePayment(long id);

//    /**
//     * Performs the asynchronous payment processing logic.
//     * This method is intended to be executed in a background thread.
//     * It waits for an external payment confirmation and updates the subscription
//     * status and expiration date accordingly.
//     *
//     * @param subscription the subscription to process
//     * @throws NullPointerException if subscription is null
//     */
//    void startAsyncPayment(Subscription subscription);
}