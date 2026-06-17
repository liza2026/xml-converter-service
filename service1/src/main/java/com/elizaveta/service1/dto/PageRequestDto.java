package com.elizaveta.service1.dto;

import lombok.Data;

@Data
public class PageRequestDto {
    private int page = 0;
    private int size = 10;
    private ConversionFilter filter = new ConversionFilter();
}