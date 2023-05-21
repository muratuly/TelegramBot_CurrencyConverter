package com.example.telegrambotcurrency.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:application.properties")
public class BotConfig {
    //Из пропертис аппликейшн берет значения бота
    @Value("${bot.name}")
    String botName;

    @Value("${bot.key}")
    String token;
}