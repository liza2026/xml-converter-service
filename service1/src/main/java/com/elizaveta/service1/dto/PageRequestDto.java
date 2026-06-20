package com.elizaveta.service1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class PageRequestDto {

    @Min(value = 0, message = "Номер страницы должен быть не меньше 0")
    private int page = 0;

    @Min(value = 1, message = "Размер страницы должен быть не меньше 1")
    private int size = 10;

    @Valid
    private ConversionFilter filter = new ConversionFilter();
}