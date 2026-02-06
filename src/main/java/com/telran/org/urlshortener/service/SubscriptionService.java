package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.entity.Subscription;

import java.util.List;

public interface SubscriptionService {
    Subscription create(Subscription subscription);

    List<Subscription> findByUserId(long userId);

    Subscription findById(long id);

    void remove(long id);

    void makePayment(long id, long userId);
}
