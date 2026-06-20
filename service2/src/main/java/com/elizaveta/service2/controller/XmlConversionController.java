package com.elizaveta.service2.controller;

import com.elizaveta.service2.service.XmlConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class XmlConversionController {

    private final XmlConversionService conversionService;

    @PostMapping(
            value = "/xml2format",
            consumes = MediaType.TEXT_XML_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> convertXml(@RequestBody String xml) {

        log.info("Получен запрос на конвертацию из XML в JSON");
        log.debug("Тело запроса: {}", xml);

        Object result = conversionService.convert(xml);

        log.info("Запрос успешно обработан, возвращаем ответ");

        return ResponseEntity.ok(Collections.singletonMap("result", result));
    }
}