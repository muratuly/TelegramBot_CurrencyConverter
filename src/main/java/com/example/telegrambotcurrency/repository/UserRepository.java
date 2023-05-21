package com.example.telegrambotcurrency.repository;

import com.example.telegrambotcurrency.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
