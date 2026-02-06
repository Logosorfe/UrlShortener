package com.telran.org.urlshortener.repository;

import com.telran.org.urlshortener.entity.UrlBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlBindingJpaRepository extends JpaRepository<UrlBinding, Long> {
    Optional<UrlBinding> findByUId(String uId);
}
