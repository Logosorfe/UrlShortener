package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.Subscription;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixNotAvailableException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UrlBindingServiceImpl implements UrlBindingService {
    private UrlBindingJpaRepository repository;

    private UserService service;

    private Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> converter;

    @Override
    public UrlBindingDTO create(UrlBindingCreateDTO dto, String pathPrefix) {
        User currentUser = service.findById(0);
        String pathSuffix = Base64.getEncoder()
                .encodeToString(dto.getOriginalUrl().getBytes(StandardCharsets.UTF_8)).substring(1, 8);
        if (pathPrefix == null || pathPrefix.isEmpty()) {
            Optional<UrlBinding> noPrefix = repository.findAll().stream()
                    .filter(ub -> ub.getUId().equals("/" + pathSuffix)).findFirst();
            if (noPrefix.isPresent()) {
                noPrefix.get().setUser(currentUser);
                return converter.entityToDto(noPrefix.get());
            }
            UrlBinding urlBindingBeforeDB = converter.dtoToEntity(dto);
            urlBindingBeforeDB.setUId("/" + pathSuffix);
            urlBindingBeforeDB.setUser(currentUser);
            return converter.entityToDto(repository.save(urlBindingBeforeDB));
        }
        Optional<Subscription> userActivePrefix = currentUser.getSubscriptions().stream()
                .filter(s -> s.getPathPrefix().equals(pathPrefix))
                .filter(s -> s.getExpirationDate().isAfter(LocalDate.now())).findFirst();
        if (userActivePrefix.isPresent()) {
            Optional<UrlBinding> withPrefix = repository.findAll().stream()
                    .filter(ub -> ub.getUId().equals(pathPrefix + "/" + pathSuffix)).findFirst();
            if (withPrefix.isPresent()) {
                withPrefix.get().setUser(currentUser);
                withPrefix.get().setCount(0L);
                return converter.entityToDto(withPrefix.get());
            }
            UrlBinding urlBindingBeforeDB = converter.dtoToEntity(dto);
            urlBindingBeforeDB.setUId(pathPrefix + "/" + pathSuffix);
            urlBindingBeforeDB.setUser(currentUser);
            return converter.entityToDto(repository.save(urlBindingBeforeDB));
        }
        throw new PathPrefixNotAvailableException("Can not create UrlBinding, as Subscription with prefix \""
                + pathPrefix + "\" is neither active nor belongs to user.");
    }

    @Override
    public List<UrlBindingDTO> findAllByUserId(long userId) {
        return service.findById(userId).getUrlBindings().stream()
                .map(ub -> converter.entityToDto(ub)).collect(Collectors.toList());
    }

    @Override
    public UrlBindingDTO find(String uId) {
        UrlBinding urlBindingFromDB = repository.findByUId(uId)
                .orElseThrow(() -> new IdNotFoundException("Unique id " + uId + " is not found."));
        return converter.entityToDto(urlBindingFromDB);
    }

    @Override
    public UrlBindingDTO reset(long id) {
        UrlBinding urlBindingFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("UrlBinding with id " + id + " is not found."));
        urlBindingFromDB.setCount(0L);
        return converter.entityToDto(urlBindingFromDB);
    }

    @Override
    public void remove(long id) {
        repository.deleteById(id);
    }
}