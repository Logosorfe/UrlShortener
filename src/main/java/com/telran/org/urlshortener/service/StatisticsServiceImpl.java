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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    private final UrlBindingJpaRepository repository;

    private final UserService service;

    private final Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> converter;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Transactional(readOnly = true)
    public Long requestsNumberByUser(long userId) {
        validateId(userId);
        User currentUser = service.getCurrentUser();
        log.debug("requestsNumberByUser requestedUserId={} by user={}", userId, currentUser.getId());
        if (currentUser.getRole() == RoleType.ROLE_USER &&
                !Objects.equals(currentUser.getId(), userId)) {
            log.warn("User {} attempted to access statistics of user {}", currentUser.getId(), userId);
            throw new AccessDeniedException("You do not have permission to access this resource");
        }
        Long result = repository.sumCountByUserId(userId);
        long count = result == null ? 0L : result;
        log.info("Statistics: userId={} totalRequests={}", userId, count);
        return count;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Transactional(readOnly = true)
    public long requestsNumberByUrlBinding(long id) {
        validateId(id);
        User currentUser = service.getCurrentUser();
        log.debug("requestsNumberByUrlBinding id={} by user={}", id, currentUser.getId());
        UrlBinding binding = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UrlBinding not found id={}", id);
                    return new IdNotFoundException("Url binding is not found");
                });
        if (currentUser.getRole() == RoleType.ROLE_USER &&
                !Objects.equals(binding.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to access UrlBinding {} belonging to {}",
                    currentUser.getId(), id, binding.getUser().getId());
            throw new AccessDeniedException("You do not have permission to access this resource");
        }
        log.info("Statistics: urlBindingId={} count={}", id, binding.getCount());
        return binding.getCount();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UrlBindingDTO> topTenRequests() {
        log.debug("topTenRequests called");
        List<UrlBindingDTO> list = repository.findTop10ByOrderByCountDesc().stream()
                .map(converter::entityToDto)
                .collect(Collectors.toList());
        log.info("topTenRequests returned {} items", list.size());
        return list;
    }

    private void validateId(long id) {
        if (id <= 0) throw new IllegalArgumentException("Id must be positive");
    }
}