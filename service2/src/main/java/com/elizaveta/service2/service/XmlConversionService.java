package com.elizaveta.service2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class XmlConversionService {

    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    public Object convert(String xml) {
        log.debug("Начало конвертации XML. Длина входной строки: {} символов", xml.length());
        log.trace("Содержимое XML: {}", xml);

        try {
            Object javaObject = xmlMapper.readValue(xml, Object.class);

            log.debug("XML успешно распарсен в Java объект типа: {}",
                    javaObject.getClass().getSimpleName());

            String jsonString = objectMapper.writeValueAsString(javaObject);

            log.debug("Результат конвертации в JSON: {}", jsonString);
            log.info("Конвертация успешно завершена");

            return javaObject;

        } catch (Exception e) {
            log.error("Ошибка конвертации XML", e);

            throw new IllegalArgumentException(
                    "Не удалось конвертировать XML: " + e.getMessage(), e);
        }
    }
}
