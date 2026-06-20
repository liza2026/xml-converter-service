package com.elizaveta.service1.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private final List<T> content;

    private final int page;

    private final int size;

    private final long totalElements;

    private final int totalPages;

    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }
}