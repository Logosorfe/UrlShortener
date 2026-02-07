package com.telran.org.urlshortener.dto;

import com.telran.org.urlshortener.model.StatusState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private Long id;
    private String pathPrefix;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private StatusState status;
}
