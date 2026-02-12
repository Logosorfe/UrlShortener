package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final UrlBindingJpaRepository repository;

    private final UserService service;

    private final Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> converter;

    @Override
    @PreAuthorize("hasRole('ADMIN', 'USER')")
    public Long requestsNumberByUser(long userId) {
        User currentUser = service.getCurrentUser();
        if (currentUser.getRole() == RoleType.ROLE_USER && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this resource");
        }
        Long result = repository.sumCountByUserId(userId);
        return result == null ? 0L : result;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public long requestsNumberByUrlBinding(long id) {
        User currentUser = service.getCurrentUser();
        UrlBinding urlBindingFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Url binding is not found"));
        if (currentUser.getRole() == RoleType.ROLE_USER
                && !currentUser.getId().equals(urlBindingFromDB.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource");
        }
        return urlBindingFromDB.getCount();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UrlBindingDTO> topTenRequests() {
        return repository.findTop10ByOrderByCountDesc().stream()
                .map(converter::entityToDto)
                .collect(Collectors.toList());
    }
}