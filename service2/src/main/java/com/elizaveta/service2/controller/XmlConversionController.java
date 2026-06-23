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

/**
 * Единственная точка входа service 2 — конвертация XML в JSON.
 * Каждый вызов фиксируется в лог-файле {@code logs/service2.log}
 * на уровне INFO (факт запроса/ответа) и DEBUG (содержимое тела).
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class XmlConversionController {

    private final XmlConversionService conversionService;

    /**
     * Принимает XML и возвращает результат конвертации в JSON.
     * <p>
     * Вход — сырой XML с {@code Content-Type: text/xml}, выход — JSON-объект
     * вида {@code {"result": <раскодированный XML>}}. Обёртка в ключ
     * {@code result} нужна, потому что сам JSON-эквивалент XML может быть как
     * объектом, так и массивом или примитивом.
     * <p>
     * Сама конвертация делегируется в {@link XmlConversionService#convert},
     * этот метод отвечает только за логирование факта запроса/ответа и
     * формирование HTTP-обёртки.
     *
     * @param xml тело запроса — XML-документ
     * @return 200 OK с телом {@code {"result": ...}}, где значение — результат
     * конвертации XML в JSON-совместимую Java-структуру
     */
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