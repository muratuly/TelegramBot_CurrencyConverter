package com.example.telegrambotcurrency.repository;

import com.example.telegrambotcurrency.model.MessagesForDb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessagesForDb, Long> {
}
