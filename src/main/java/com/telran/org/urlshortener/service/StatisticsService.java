package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingDTO;

import java.util.List;

public interface StatisticsService {
    Long requestsNumberByUser(long userId);

    long requestsNumberByUrlBinding(long urlBindingId);

    List<UrlBindingDTO> topTenRequests();
}
