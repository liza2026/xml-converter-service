package com.elizaveta.service1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequestDTO {

    private Long id;
    private String jsonResult;
    private LocalDateTime requestDate;
    private Long processingTimeMs;
    private Integer xmlTagsCount;
    private Integer jsonKeysCount;
}