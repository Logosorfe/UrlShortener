package com.telran.org.urlshortener.mapper;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.UrlBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between UrlBinding entities and UrlBinding DTOs.
 * Responsible for constructing the full short URL using the configured base URL.
 */
@Component
public class UrlBindingDtoMapper implements Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> {
    private final String baseUrl;

    public UrlBindingDtoMapper(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }
    /**
     * Converts a UrlBinding entity into a UrlBindingDTO.
     * Builds the full short URL by combining the base URL with the UID.
     *
     * @param urlBinding the entity to convert
     * @return the corresponding DTO containing the short URL
     */
    @Override
    public UrlBindingDTO entityToDto(UrlBinding urlBinding) {
        String shortUrl = baseUrl.endsWith("/") ? baseUrl + urlBinding.getUid()
                .replaceFirst("^/", "") : baseUrl + "/" + urlBinding.getUid()
                .replaceFirst("^/", "");
        return UrlBindingDTO.builder()
                .id(urlBinding.getId())
                .originalUrl(urlBinding.getOriginalUrl())
                .shortUrl(shortUrl)
                .count(urlBinding.getCount())
                .build();
    }
    /**
     * Converts a UrlBindingCreateDTO into a UrlBinding entity.
     *
     * @param dto the DTO containing the original URL
     * @return the new UrlBinding entity
     */
    @Override
    public UrlBinding dtoToEntity(UrlBindingCreateDTO dto) {
        return new UrlBinding(dto.getOriginalUrl());
    }
}