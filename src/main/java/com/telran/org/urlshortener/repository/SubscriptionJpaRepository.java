package com.telran.org.urlshortener.repository;

import com.telran.org.urlshortener.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, Long> {
}
