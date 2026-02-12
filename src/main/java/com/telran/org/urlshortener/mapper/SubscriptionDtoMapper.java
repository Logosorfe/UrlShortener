package com.telran.org.urlshortener.mapper;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionDtoMapper implements Converter<Subscription, SubscriptionCreateDTO, SubscriptionDTO> {
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

    @Override
    public Subscription dtoToEntity(SubscriptionCreateDTO dto) {
        return new Subscription(dto.getPathPrefix());
    }
}