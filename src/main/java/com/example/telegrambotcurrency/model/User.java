package com.example.telegrambotcurrency.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity(name = "usersDataTable")
//Класс позволяющий сохранить пользователей,
// которые когда либо пользовались ботом
public class User {

    @Id
    private Long chatId;

    private String firsName;

    private String lastName;

    private String username;

    private Timestamp registeredAt;
}