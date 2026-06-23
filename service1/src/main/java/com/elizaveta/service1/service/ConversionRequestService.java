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

/**
 * Главный оркестратор service 1: связывает воедино вызов service 2 и
 * сохранение результата в PostgreSQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionRequestService {

    private final Service2Client service2Client;
    private final ConversionRequestDAO conversionRequestDao;
    private final ConversionRequestMapper conversionRequestMapper;

    private final ObjectMapper objectMapper;

    /**
     * Выполняет полный цикл обработки одного запроса на конвертацию:
     * вызов service 2 -> подсчёт статистики -> сохранение в БД.
     * <p>
     * Время обработки ({@code processingTimeMs}) считается от начала метода
     * до момента сериализации — то есть включает в себя сетевую задержку
     * похода в service 2, но не включает время записи в БД. Вся операция
     * выполняется в рамках одной транзакции {@link Transactional}: если
     * сохранение в БД не удастся, клиент получит ошибку, даже если service 2
     * уже успешно сконвертировал XML.
     *
     * @param xml исходный XML, полученный контроллером от клиента
     * @return DTO с результатом конвертации, присвоенным id записи в БД,
     * временем обработки и статистикой по тегам/ключам
     * @throws IllegalArgumentException если входной XML некорректен
     *                                  (выбрасывается при подсчёте тегов до похода в service 2)
     * @throws RuntimeException         если service 2 недоступен или вернул ошибку,
     *                                  либо если результат не удалось сериализовать в JSON-строку
     *                                  для сохранения в БД
     */
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

    /**
     * Возвращает страницу истории обработанных запросов из БД с учётом
     * фильтра.
     * <p>
     * Делегирует выборку и подсчёт общего количества записей в
     * {@link ConversionRequestDAO}: данные и общее количество элементов
     * запрашиваются двумя отдельными запросами к БД (один — за срезом
     * данных через {@code LIMIT}/{@code OFFSET}, другой — за {@code COUNT}),
     * оба — в рамках одной read-only транзакции, чтобы оба запроса видели
     * согласованный снимок данных. Сущности {@link ConversionRequestDTO},
     * полученные из DAO, преобразуются в {@link ConversionRequestDTO} через
     * {@link ConversionRequestMapper}.
     *
     * @param filter критерии фильтрации (диапазоны даты запроса, времени
     *               обработки, количества XML-тегов и JSON-ключей);
     *               все непустые условия фильтра комбинируются через AND
     * @param page   номер страницы, начиная с 0
     * @param size   размер страницы
     * @return страница с данными, текущими параметрами пагинации и
     * вычисленным общим количеством страниц
     */
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