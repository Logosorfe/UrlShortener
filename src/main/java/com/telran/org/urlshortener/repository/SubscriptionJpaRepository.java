package com.telran.org.urlshortener.repository;

import com.telran.org.urlshortener.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByPathPrefix(String pathPrefix);

    List<Subscription> findByUserId(Long userId);
}