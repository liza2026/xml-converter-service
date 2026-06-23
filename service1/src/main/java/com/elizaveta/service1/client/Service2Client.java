package com.elizaveta.service1.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP-клиент service 1 для обращения к service 2.
 * <p>
 * В Docker Compose service 2 виден по имени контейнера ({@code service2}),
 * адрес берётся из {@code service2.base-url} (переменная окружения
 * {@code SERVICE2_BASE_URL}), что позволяет service 1 не знать жёстко
 * прописанный хост/порт. Связь синхронная: service 1 блокируется на время
 * ответа service 2, отдельного circuit breaker или retry нет — если service 2
 * недоступен или вернул ошибку, запрос к {@code /request} в service 1
 * целиком завершится ошибкой и ничего не будет сохранено в БД.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Service2Client {

    private final RestTemplate restTemplate;

    @Value("${service2.base-url}")
    private String service2BaseUrl;

    public Object convertXmlToJson(String xml) {

        String url = service2BaseUrl + "/xml2format";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "xml", StandardCharsets.UTF_8));

        HttpEntity<String> request = new HttpEntity<>(xml, headers);

        log.debug("Отправляем запрос в Service 2: {}", url);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
            );

            log.debug("Получен ответ от Service 2, статус: {}", response.getStatusCode());

            return response.getBody().get("result");

        } catch (Exception e) {
            log.error("Ошибка при обращении к Service 2: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить ответ от Service 2: " + e.getMessage(), e);
        }
    }
}