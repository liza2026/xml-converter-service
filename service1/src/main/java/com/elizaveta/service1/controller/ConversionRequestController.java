package com.elizaveta.service1.controller;

import com.elizaveta.service1.dto.*;
import com.elizaveta.service1.service.ConversionRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConversionRequestController {

    private final ConversionRequestService conversionRequestService;

    @PostMapping(
            value = "/request",
            consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ConversionResponseDTO> handleRequest(
            @RequestBody String xml
    ) {

        log.info("Получен запрос на /request");
        log.debug("Тело запроса: {}", xml);

        ConversionResponseDTO response = conversionRequestService.processRequest(xml);

        log.info("Запрос успешно обработан, id={}", response.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/page",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResponseDTO<ConversionRequestDTO>> getPage(
            @Valid @RequestBody(required = false) PageRequestDTO pageRequest
    ) {

        if (pageRequest == null) {
            pageRequest = new PageRequestDTO();
        }

        log.info("Получен запрос на /page: page={}, size={}",
                pageRequest.getPage(),
                pageRequest.getSize());

        ConversionFilterDTO filter = pageRequest.getFilter();

        log.info("Фильтры: {}", filter);

        PageResponseDTO<ConversionRequestDTO> response =
                conversionRequestService.getPage(filter, pageRequest.getPage(), pageRequest.getSize());

        log.info("Успешно возвращено {} записей из {} (страница {} из {})",
                response.getContent().size(),
                response.getTotalElements(),
                response.getPage(),
                response.getTotalPages());

        return ResponseEntity.ok(response);
    }
}