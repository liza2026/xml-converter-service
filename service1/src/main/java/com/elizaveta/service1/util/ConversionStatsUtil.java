package com.elizaveta.service1.util;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ConversionStatsUtil {

    private ConversionStatsUtil() {
    }

    public static int countXmlTags(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
            );

            return document.getElementsByTagName("*").getLength();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка подсчёта тегов XML: " + e.getMessage(), e);
        }
    }

    public static int countJsonKeys(Object json) {

        if (json instanceof Map<?, ?> map) {
            int count = map.size();
            for (Object value : map.values()) {
                count += countJsonKeys(value);
            }
            return count;
        }

        if (json instanceof List<?> list) {
            int count = 0;
            for (Object item : list) {
                count += countJsonKeys(item);
            }
            return count;
        }

        return 0;
    }
}