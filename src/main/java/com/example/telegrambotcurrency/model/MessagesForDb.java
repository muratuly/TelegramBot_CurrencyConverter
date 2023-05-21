package com.example.telegrambotcurrency.model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "telegram_messages")
//Класс для сохранения сообщении в базе данных
public class MessagesForDb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idChat;

    private String text;

    private LocalDateTime messageTime;
}