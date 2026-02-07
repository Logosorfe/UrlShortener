package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;

import java.util.List;

public interface UrlBindingService {
    UrlBindingDTO create(UrlBindingCreateDTO dto, String pathPrefix);

    List<UrlBindingDTO> findAllByUserId(long userId);

    UrlBindingDTO find(String uId);

    UrlBindingDTO reset(long id);

    void remove(long id);
}
