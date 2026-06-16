package com.elizaveta.service1.controller;

import com.elizaveta.service1.dto.ConversionResponse;
import com.elizaveta.service1.service.ConversionRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.elizaveta.service1.dto.ConversionFilter;
import com.elizaveta.service1.dto.ConversionRequestDto;
import com.elizaveta.service1.dto.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConversionRequestController {

    private final ConversionRequestService conversionRequestService;

    @PostMapping(
            value = "/request",
            consumes = MediaType.TEXT_XML_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ConversionResponse> handleRequest(@RequestBody String xml) {

        log.info("Получен запрос на /request");
        log.debug("Тело запроса: {}", xml);

        ConversionResponse response = conversionRequestService.processRequest(xml);

        log.info("Запрос успешно обработан, id={}", response.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponse<ConversionRequestDto>> getPage(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime requestDateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime requestDateTo,

            @RequestParam(required = false) Long processingTimeMin,
            @RequestParam(required = false) Long processingTimeMax,

            @RequestParam(required = false) Integer xmlTagsCountMin,
            @RequestParam(required = false) Integer xmlTagsCountMax,

            @RequestParam(required = false) Integer jsonKeysCountMin,
            @RequestParam(required = false) Integer jsonKeysCountMax
    ) {

        log.info("Получен запрос на /page: page={}, size={}", page, size);
        log.info("Фильтры: requestDateFrom={}, requestDateTo={}, " +
                        "processingTimeMin={}, processingTimeMax={}, " +
                        "xmlTagsCountMin={}, xmlTagsCountMax={}, " +
                        "jsonKeysCountMin={}, jsonKeysCountMax={}",
                requestDateFrom, requestDateTo,
                processingTimeMin, processingTimeMax,
                xmlTagsCountMin, xmlTagsCountMax,
                jsonKeysCountMin, jsonKeysCountMax);

        ConversionFilter filter = new ConversionFilter(
                requestDateFrom, requestDateTo,
                processingTimeMin, processingTimeMax,
                xmlTagsCountMin, xmlTagsCountMax,
                jsonKeysCountMin, jsonKeysCountMax
        );

        PageResponse<ConversionRequestDto> response =
                conversionRequestService.getPage(filter, page, size);

        log.info("Успешно возвращено {} записей из {} (страница {} из {})",
                response.getContent().size(),
                response.getTotalElements(),
                response.getPage(),
                response.getTotalPages());

        return ResponseEntity.ok(response);
    }
}