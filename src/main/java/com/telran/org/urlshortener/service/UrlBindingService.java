package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.entity.UrlBinding;

import java.util.List;

public interface UrlBindingService {
    UrlBinding create(UrlBinding urlBinding, String pathPrefix);

    List<UrlBinding> findAllByUserId(long userId);

    UrlBinding find(String uId);

    UrlBinding reset(long id);

    void remove(long id);
}
