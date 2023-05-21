package com.example.telegrambotcurrency.integration;

import com.example.telegrambotcurrency.integration.payload.request.ConvertRequest;
import com.example.telegrambotcurrency.integration.payload.response.ConvertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ConverterApi {

    private final RestTemplate restTemplate;

    //Конвертер валюты с сайта apilayer и ключ хранится в пропертис
    @Value("${converter.base-url}")
    private String converterBaseUrl;

    @Value("${converter.api-key}")
    private String apiKey;

    public ConvertResponse convert(ConvertRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apiKey", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ConvertResponse> response = restTemplate.exchange(converterBaseUrl + "/convert?from=" +
                        request.getFrom() + "&to=" + request.getTo() + "&amount=" + request.getAmount(), HttpMethod.GET, entity,
                new ParameterizedTypeReference<ConvertResponse>() {
                });
        return response.getBody();
    }
}