package com.telran.org.urlshortener.mapper;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.UrlBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlBindingDtoMapper implements Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> {
    private final String baseUrl;

    public UrlBindingDtoMapper(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public UrlBindingDTO entityToDto(UrlBinding urlBinding) {
        String shortUrl = baseUrl.endsWith("/") ? baseUrl + urlBinding.getUId()
                .replaceFirst("^/", "") : baseUrl + "/" + urlBinding.getUId()
                .replaceFirst("^/", "");
        return UrlBindingDTO.builder()
                .id(urlBinding.getId())
                .originalUrl(urlBinding.getOriginalUrl())
                .shortUrl(shortUrl)
                .count(urlBinding.getCount())
                .build();
    }

    @Override
    public UrlBinding dtoToEntity(UrlBindingCreateDTO dto) {
        return new UrlBinding(dto.getOriginalUrl());
    }
}