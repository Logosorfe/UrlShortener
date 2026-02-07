package com.telran.org.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlBindingDTO {
    private Long id;
    private String originalUrl;
    private String shortUrl;
    private Long count;
}
