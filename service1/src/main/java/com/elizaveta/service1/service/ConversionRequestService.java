package com.elizaveta.service1.service;

import com.elizaveta.service1.client.Service2Client;
import com.elizaveta.service1.dao.ConversionRequestDAO;
import com.elizaveta.service1.dto.ConversionFilterDTO;
import com.elizaveta.service1.dto.ConversionRequestDTO;
import com.elizaveta.service1.dto.ConversionResponseDTO;
import com.elizaveta.service1.dto.PageResponseDTO;
import com.elizaveta.service1.entity.ConversionRequest;
import com.elizaveta.service1.mapper.ConversionRequestMapper;
import com.elizaveta.service1.util.ConversionStatsUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionRequestService {

    private final Service2Client service2Client;
    private final ConversionRequestDAO conversionRequestDao;
    private final ConversionRequestMapper conversionRequestMapper;

    private final ObjectMapper objectMapper;

    @Transactional
    public ConversionResponseDTO processRequest(String xml) {

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

        return new ConversionResponseDTO(
                saved.getId(),
                jsonResult,
                requestDate,
                processingTimeMs,
                xmlTagsCount,
                jsonKeysCount
        );
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ConversionRequestDTO> getPage(ConversionFilterDTO filter, int page, int size) {

        List<ConversionRequest> content = conversionRequestDao.findContent(filter, page, size);
        long totalElements = conversionRequestDao.countTotal(filter);

        List<ConversionRequestDTO> dtos = content.stream()
                .map(conversionRequestMapper::toDto)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(dtos, page, size, totalElements);
    }
}