package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final UrlBindingJpaRepository repository;

    private final Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> converter;

    @Override
    @PreAuthorize("hasRole('ADMIN', 'USER')")
    public Long requestsNumberByUser(long userId) {
        Long result = repository.sumCountByUserId(userId);
        return result == null ? 0L : result;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN', 'USER')")
    public long requestsNumberByUrlBinding(long id) {
        return repository.findById(id).map(UrlBinding::getCount)
                .orElseThrow(() -> new IdNotFoundException("Url binding is not found"));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UrlBindingDTO> topTenRequests() {
        return repository.findTop10ByOrderByCountDesc().stream()
                .map(converter::entityToDto)
                .collect(Collectors.toList());
    }
}
