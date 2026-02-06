package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    UserService service;

    @Override
    public Long requestsNumberByUser() {
        return service.findById(0).getUrlBindings().stream()
                .map(UrlBinding::getCount).reduce(0L, Long::sum);
    }

    @Override
    public Long requestsNumberByUrlBinding(Long urlBindingId) {
        return service.findAll().stream().flatMap(u -> u.getUrlBindings().stream())
                .filter(ub -> ub.getId().equals(urlBindingId)).map(UrlBinding::getCount).findFirst()
                .orElseThrow(() -> new IdNotFoundException("UrlBinding is not found"));
    }

    @Override
    public List<UrlBinding> topTenRequests() {
        return service.findAll().stream().flatMap(u -> u.getUrlBindings().stream())
                .sorted((u1, u2) -> u1.getCount().compareTo(u2.getCount()))
                .limit(10).collect(Collectors.toList());
    }
}
