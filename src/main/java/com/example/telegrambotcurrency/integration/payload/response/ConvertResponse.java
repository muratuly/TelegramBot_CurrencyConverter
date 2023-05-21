package com.example.telegrambotcurrency.integration.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvertResponse {
    private Boolean success;
    private Double result;
}