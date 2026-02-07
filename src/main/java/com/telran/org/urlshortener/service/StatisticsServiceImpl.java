package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private UrlBindingJpaRepository repository;

    private Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> converter;

    @Override
    public long requestsNumberByUserId(long userId) {
        return repository.findAll().stream().filter(ub -> ub.getUser().getId() == userId)
                .map(UrlBinding::getCount).reduce(0L, Long::sum);
    }

    @Override
    public long requestsNumberByUrlBinding(long urlBindingId) {
        return repository.findById(urlBindingId).map(UrlBinding::getCount)
                .orElseThrow(() -> new IdNotFoundException("UrlBinding is not found"));
    }

    @Override
    public List<UrlBindingDTO> topTenRequests() {
        return repository.findAll().stream()
                .map(ub -> converter.entityToDto(ub))
                .sorted((u1, u2) -> u1.getCount().compareTo(u2.getCount()))
                .limit(10).collect(Collectors.toList());
    }
}
