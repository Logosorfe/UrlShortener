package com.telran.org.urlshortener.dto;

import com.telran.org.urlshortener.model.StatusState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object representing a subscription in API responses.
 * A subscription grants a user the right to use a specific path prefix
 * when generating shortened URLs.
 *
 * <p>This DTO exposes subscription metadata such as creation date,
 * expiration date, and current status.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    /**
     * Unique identifier of the subscription.
     */
    private Long id;
    /**
     * Path prefix associated with the subscription. * Must be unique across all users.
     */
    private String pathPrefix;
    /**
     * Date when the subscription was created.
     */
    private LocalDate creationDate;
    /**
     * Date when the subscription expires. * May be null if the subscription has not been paid yet.
     */
    private LocalDate expirationDate;
    /**
     * Current status of the subscription (e.g., UNPAID or PAID).
     */
    private StatusState status;
}