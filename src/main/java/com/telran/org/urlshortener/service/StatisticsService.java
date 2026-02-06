package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.entity.UrlBinding;

import java.util.List;

public interface StatisticsService {
    Long requestsNumberByUser();

    Long requestsNumberByUrlBinding(Long urlBindingId);

    List<UrlBinding> topTenRequests();
}
