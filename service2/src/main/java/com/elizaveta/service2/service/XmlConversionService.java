package com.elizaveta.service2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Реализует двухэтапную конвертацию XML -> (XSLT) -> XML -> JSON.
 * <p>
 * <b>Этап 1 — XSLT-преобразование</b> (делегируется в {@link XsltTransformService}).
 * <b>Этап 2 — XML -> JSON</b>: преобразованный XML читается Jackson {@link XmlMapper}
 *  * в дерево Java-объектов, которое контроллер сериализует в JSON.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XmlConversionService {

    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;
    private final XsltTransformService xsltTransformService;

    /**
     * Преобразует XML-документ в JSON-совместимую Java-структуру.
     * <p>
     * Сначала применяется XSLT (листовые теги → атрибуты), затем
     * результирующий XML читается в обобщённое дерево объектов
     * ({@code Map}/{@code List}/примитивы).
     *
     * @param xml исходный XML
     * @return результат конвертации — объект, который Jackson способен
     * сериализовать в JSON (как правило, {@code LinkedHashMap})
     * @throws IllegalArgumentException если XML невалиден или XSLT-преобразование завершилось ошибкой
     */
    public Object convert(String xml) {
        log.debug("Начало конвертации XML. Длина входной строки: {} символов", xml.length());
        log.trace("Содержимое XML: {}", xml);

        try {
            String transformedXml = xsltTransformService.transform(xml);

            Object javaObject = xmlMapper.readValue(transformedXml, Object.class);
            log.debug("XML успешно распарсен в Java-объект типа: {}",
                    javaObject.getClass().getSimpleName());

            String jsonString = objectMapper.writeValueAsString(javaObject);
            log.debug("Результат конвертации в JSON: {}", jsonString);
            log.info("Конвертация успешно завершена");

            return javaObject;

        } catch (Exception e) {
            log.error("Внутренняя ошибка при обработке преобразованного XML", e);
            throw new RuntimeException("Внутренняя ошибка конвертации", e);
        }
    }
}
