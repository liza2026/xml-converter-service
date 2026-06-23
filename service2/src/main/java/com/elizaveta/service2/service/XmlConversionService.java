package com.elizaveta.service2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Реализует саму конвертацию XML -> JSON.
 * <p>
 * Структура входного XML напрямую
 * становится структурой выходного JSON, без переименования полей или
 * изменения вложенности.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XmlConversionService {

    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    /**
     * Преобразует XML-документ в JSON-совместимую Java-структуру
     * (вложенные {@code Map}/{@code List}/примитивы), сохраняя исходную
     * иерархию тегов и атрибутов один в один.
     * <p>
     * Используются два отдельных Jackson-маппера: {@link XmlMapper} читает
     * XML и строит из него обобщённое дерево объектов ({@code Object.class}),
     * а {@link ObjectMapper} затем сериализует это дерево в JSON-строку —
     * она используется только для лога ({@code log.debug}), чтобы видеть
     * итоговый JSON.
     * <p>
     * Любая ошибка парсинга (невалидный XML, не соответствующий XML-спецификации
     * ввод) перехватывается и оборачивается в {@link IllegalArgumentException}
     * с понятным сообщением — это проблема в содержимом запроса, а не
     * во внутренней логике сервиса.
     *
     * @param xml исходный XML
     * @return результат конвертации — объект, который Jackson способен
     * сериализовать в JSON (как правило, {@code LinkedHashMap})
     * @throws IllegalArgumentException если переданная строка не является
     *                                  корректным XML и не может быть распарсена
     */
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
