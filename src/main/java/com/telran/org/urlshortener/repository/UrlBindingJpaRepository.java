package com.telran.org.urlshortener.repository;

import com.telran.org.urlshortener.entity.UrlBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UrlBindingJpaRepository extends JpaRepository<UrlBinding, Long> {
    Optional<UrlBinding> findByUId(String uId);

    List<UrlBinding> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(u.count), 0) FROM UrlBinding u WHERE u.user.id = :userId")
    Long sumCountByUserId(@Param("userId") Long userId);

    List<UrlBinding> findTop10ByOrderByCountDesc();
}