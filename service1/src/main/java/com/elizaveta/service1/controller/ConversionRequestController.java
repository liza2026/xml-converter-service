package com.elizaveta.service1.controller;

import com.elizaveta.service1.dto.*;
import com.elizaveta.service1.service.ConversionRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Клиент отправляет XML и получает JSON либо запрашивает историю
 * обработанных запросов. Делегирует всю оркестрацию
 * {@link ConversionRequestService}, контроллер отвечает только за HTTP-контракт
 * (content negotiation, статусы, логирование факта обращения).
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ConversionRequestController {

    private final ConversionRequestService conversionRequestService;

    /**
     * Принимает XML от клиента, конвертирует его в JSON через service 2 и
     * сохраняет результат вместе со статистикой в БД.
     * <p>
     * Сам по себе этот метод не обращается ни к service 2, ни к БД — это
     * входная точка цепочки {@link ConversionRequestService#processRequest},
     * которая внутри делает HTTP-запрос к service 2 (конвертация) и затем
     * пишет результат в PostgreSQL (сохранение истории). Если любой из этих
     * шагов завершится ошибкой, она долетит сюда как необработанное
     * исключение и будет преобразована в {@code 4xx}/{@code 5xx} ответ
     * через {@code GlobalExceptionHandler}.
     *
     * @param xml тело запроса в формате {@code text/xml} или
     *            {@code application/xml} — конвертируемый XML-документ
     * @return 200 OK с {@link ConversionResponseDTO}: результат конвертации,
     * присвоенный id записи в БД, время обработки и статистика
     * по количеству тегов/ключей
     */
    @PostMapping(
            value = "/request",
            consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ConversionResponseDTO> handleRequest(
            @RequestBody String xml
    ) {

        log.info("Получен запрос на /request");
        log.debug("Тело запроса: {}", xml);

        ConversionResponseDTO response = conversionRequestService.processRequest(xml);

        log.info("Запрос успешно обработан, id={}", response.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Возвращает постраничный список ранее обработанных запросов из БД
     * с опциональной фильтрацией.
     * <p>
     * К service 2 не обращается — это чисто работа с БД: тело запроса
     * (номер страницы, размер и фильтр) транслируется в Criteria-запрос
     * внутри {@link ConversionRequestService#getPage}, который читает
     * напрямую из таблицы {@code conversion_requests} через
     * {@code ConversionRequestDAO}. Фильтр поддерживает одновременное
     * применение нескольких условий (диапазон даты запроса, диапазон
     * времени обработки, диапазон количества XML-тегов, диапазон
     * количества JSON-ключей).
     * <p>
     * Тело запроса необязательно: при отсутствии используются значения
     * по умолчанию из {@link PageRequestDTO}.
     *
     * @param pageRequest номер страницы, размер страницы и фильтр;
     *                    может быть {@code null}, если тело запроса не передано
     * @return 200 OK с {@link PageResponseDTO}: содержимое страницы,
     * метаданные пагинации (текущая страница, размер,
     * общее количество элементов и страниц)
     */
    @PostMapping(
            value = "/page",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResponseDTO<ConversionRequestDTO>> getPage(
            @Valid @RequestBody(required = false) PageRequestDTO pageRequest
    ) {

        if (pageRequest == null) {
            pageRequest = new PageRequestDTO();
        }

        log.info("Получен запрос на /page: page={}, size={}",
                pageRequest.getPage(),
                pageRequest.getSize());

        ConversionFilterDTO filter = pageRequest.getFilter();

        log.info("Фильтры: {}", filter);

        PageResponseDTO<ConversionRequestDTO> response =
                conversionRequestService.getPage(filter, pageRequest.getPage(), pageRequest.getSize());

        log.info("Успешно возвращено {} записей из {} (страница {} из {})",
                response.getContent().size(),
                response.getTotalElements(),
                response.getPage(),
                response.getTotalPages());

        return ResponseEntity.ok(response);
    }
}