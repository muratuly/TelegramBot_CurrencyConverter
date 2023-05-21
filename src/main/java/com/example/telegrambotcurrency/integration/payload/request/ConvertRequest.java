package com.example.telegrambotcurrency.integration.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConvertRequest {
    private String from;
    private String to;
    private Double amount;
}