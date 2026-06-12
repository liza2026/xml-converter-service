package com.elizaveta.service1.client;

import com.elizaveta.service1.dto.Service2Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> request = new HttpEntity<>(xml, headers);

        log.debug("Отправляем запрос в Service 2: {}", url);

        try {
            ResponseEntity<Service2Response> response = restTemplate.postForEntity(
                    url,
                    request,
                    Service2Response.class
            );

            log.debug("Получен ответ от Service 2, статус: {}", response.getStatusCode());

            return response.getBody().getResult();

        } catch (Exception e) {
            log.error("Ошибка при обращении к Service 2: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить ответ от Service 2: " + e.getMessage(), e);
        }
    }
}