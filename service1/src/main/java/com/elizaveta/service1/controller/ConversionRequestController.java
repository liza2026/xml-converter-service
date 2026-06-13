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

        try {
            ConversionResponse response = conversionRequestService.processRequest(xml);

            log.info("Запрос успешно обработан, id={}", response.getId());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Ошибка обработки запроса: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}