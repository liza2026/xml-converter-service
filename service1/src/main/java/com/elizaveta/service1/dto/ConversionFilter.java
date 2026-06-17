package com.elizaveta.service1.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversionFilter {

    private LocalDateTime requestDateFrom;

    private LocalDateTime requestDateTo;

    private Long processingTimeMin;

    private Long processingTimeMax;

    private Integer xmlTagsCountMin;

    private Integer xmlTagsCountMax;

    private Integer jsonKeysCountMin;

    private Integer jsonKeysCountMax;
}