package com.telran.org.urlshortener.mapper;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.UrlBinding;
import org.springframework.stereotype.Component;

@Component
public class UrlBindingDtoMapper implements Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> {

    @Override
    public UrlBindingDTO entityToDto(UrlBinding urlBinding) {
        return UrlBindingDTO.builder()
                .id(urlBinding.getId())
                .originalUrl(urlBinding.getOriginalUrl())
                .shortUrl(urlBinding.getBaseUrl() + urlBinding.getUId())
                .count(urlBinding.getCount()).build();
    }

    @Override
    public UrlBinding dtoToEntity(UrlBindingCreateDTO dto) {
        return new UrlBinding(dto.getOriginalUrl());
    }
}