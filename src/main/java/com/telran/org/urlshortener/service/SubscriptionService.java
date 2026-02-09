package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;

import java.util.List;

public interface SubscriptionService {
    SubscriptionDTO create(SubscriptionCreateDTO dto);

    List<SubscriptionDTO> findByUserId(long userId);

    SubscriptionDTO findById(long id);

    void delete(long id);

    void makePayment(long id, long userId);
}
