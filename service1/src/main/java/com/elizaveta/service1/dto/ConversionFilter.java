package com.elizaveta.service1.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Data
public class ConversionFilter {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime requestDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime requestDateTo;

    @Min(value = 0, message = "Время обработки должно быть не меньше 0 мс")
    private Long processingTimeMin;

    @Min(value = 0, message = "Время обработки должно быть не меньше 0 мс")
    private Long processingTimeMax;

    @Min(value = 0, message = "Количество тегов должно быть не меньше 0")
    private Integer xmlTagsCountMin;

    @Min(value = 0, message = "Количество тегов должно быть не меньше 0")
    private Integer xmlTagsCountMax;

    @Min(value = 0, message = "Количество ключей должно быть не меньше 0")
    private Integer jsonKeysCountMin;

    @Min(value = 0, message = "Количество ключей должно быть не меньше 0")
    private Integer jsonKeysCountMax;
}