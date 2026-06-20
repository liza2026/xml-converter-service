package com.elizaveta.service1.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@Data
public class PageRequestDto {

    @Min(value = 0, message = "Номер страницы должен быть не меньше 0")
    private int page = 0;

    @Min(value = 1, message = "Размер страницы должен быть не меньше 1")
    private int size = 10;

    @Valid
    private ConversionFilter filter = new ConversionFilter();
}