package com.elizaveta.service1.service;

import com.elizaveta.service1.client.Service2Client;
import com.elizaveta.service1.dao.ConversionRequestDao;
import com.elizaveta.service1.dto.ConversionResponse;
import com.elizaveta.service1.entity.ConversionRequest;
import com.elizaveta.service1.util.ConversionStatsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionRequestService {

    private final Service2Client service2Client;
    private final ConversionRequestDao conversionRequestDao;

    private final ObjectMapper objectMapper;

    public ConversionResponse processRequest(String xml) {

        long startTime = System.currentTimeMillis();

        int xmlTagsCount = ConversionStatsUtil.countXmlTags(xml);
        log.debug("Количество тегов в XML: {}", xmlTagsCount);

        Object jsonResult = service2Client.convertXmlToJson(xml);
        log.debug("Получен результат конвертации от Service 2");

        int jsonKeysCount = ConversionStatsUtil.countJsonKeys(jsonResult);
        log.debug("Количество ключей в JSON: {}", jsonKeysCount);

        String jsonResultString;
        try {
            jsonResultString = objectMapper.writeValueAsString(jsonResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации результата в JSON: " + e.getMessage(), e);
        }

        long processingTimeMs = System.currentTimeMillis() - startTime;
        log.debug("Время обработки: {} мс", processingTimeMs);

        LocalDateTime requestDate = LocalDateTime.now();

        ConversionRequest entity = new ConversionRequest(
                jsonResultString,
                requestDate,
                processingTimeMs,
                xmlTagsCount,
                jsonKeysCount
        );

        ConversionRequest saved = conversionRequestDao.save(entity);
        log.info("Запрос обработан и сохранён в БД, id={}", saved.getId());

        return new ConversionResponse(
                saved.getId(),
                jsonResult,
                requestDate,
                processingTimeMs,
                xmlTagsCount,
                jsonKeysCount
        );
    }
}