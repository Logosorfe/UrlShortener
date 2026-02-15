package com.telran.org.urlshortener.mapper;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.entity.Subscription;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Subscription entities and Subscription DTOs.
 * Used to expose subscription data through the API.
 */
@Component
public class SubscriptionDtoMapper implements Converter<Subscription, SubscriptionCreateDTO, SubscriptionDTO> {
    /**
     * Converts a Subscription entity into a SubscriptionDTO.
     *
     * @param subscription the entity to convert
     * @return the corresponding DTO
     */
    @Override
    public SubscriptionDTO entityToDto(Subscription subscription) {
        return SubscriptionDTO.builder()
                .id(subscription.getId())
                .pathPrefix(subscription.getPathPrefix())
                .creationDate(subscription.getCreationDate())
                .expirationDate(subscription.getExpirationDate())
                .status(subscription.getStatus())
                .build();
    }
    /**
     * Converts a SubscriptionCreateDTO into a Subscription entity.
     * @param dto the DTO containing subscription creation data
     * @return the new Subscription entity
     */
    @Override
    public Subscription dtoToEntity(SubscriptionCreateDTO dto) {
        return new Subscription(dto.getPathPrefix());
    }
}