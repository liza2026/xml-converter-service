package com.elizaveta.service2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Применяет XSLT-преобразование к входному XML.
 * <p>
 * Таблица стилей {@code transform.xslt} загружается из classpath
 * (src/main/resources) один раз при создании бина и кешируется
 * в поле {@link #templates}
 * <p>
 * Листовые дочерние элементы любого узла
 * становятся его атрибутами, вложенные структуры обрабатываются рекурсивно.
 */
@Slf4j
@Service
public class XsltTransformService {

    private static final String XSLT_PATH = "transform.xslt";

    /**
     * Скомпилированная таблица стилей.
     */
    private final Templates templates;

    public XsltTransformService() {

        TransformerFactory factory = TransformerFactory.newInstance();

        ClassPathResource resource = new ClassPathResource(XSLT_PATH);
        try {
            resource.getInputStream().close();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Файл XSLT-таблицы стилей не найден в classpath: " + XSLT_PATH
                            + ". Убедитесь, что файл находится в src/main/resources/", e);
        }

        try {
            Source xsltSource = new StreamSource(resource.getInputStream());
            this.templates = factory.newTemplates(xsltSource);
            log.info("XSLT-таблица стилей '{}' успешно загружена", XSLT_PATH);
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(
                    "Ошибка компиляции XSLT-таблицы стилей '" + XSLT_PATH
                            + "'. Проверьте синтаксис XSLT-файла", e);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удалось прочитать XSLT-файл при компиляции: " + XSLT_PATH, e);
        }
    }

    /**
     * Применяет XSLT-преобразование к переданному XML.
     *
     * @param xml исходный XML-документ
     * @return XML-строка после преобразования
     * @throws IllegalArgumentException если пользователь передал невалидный XML
     * @throws RuntimeException         если произошла внутренняя ошибка XSLT-движка
     */
    public String transform(String xml) {

        log.debug("Начало XSLT-преобразования. Длина входной строки: {} символов", xml.length());
        log.trace("Входной XML: {}", xml);

        try {
            Transformer transformer = templates.newTransformer();

            Source source = new StreamSource(new StringReader(xml));
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);

            transformer.transform(source, result);

            String output = writer.toString();
            log.debug("XSLT-преобразование завершено. Результат: {}", output);
            log.info("XSLT-преобразование успешно выполнено");

            return output;

        } catch (TransformerException e) {
            if (e.getCause() instanceof org.xml.sax.SAXParseException) {
                log.warn("Невалидный XML от пользователя: {}", e.getMessage());
                throw new IllegalArgumentException(
                        "Невалидный XML: " + e.getMessage(), e);
            }

            log.error("Внутренняя ошибка XSLT-движка", e);
            throw new RuntimeException("Внутренняя ошибка XSLT-преобразования", e);
        }
    }
}