package com.example.springdata.spring_db_demo.repository;

import com.example.springdata.spring_db_demo.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);

    List<User> findAll();

    User save(User user);

    void deleteById(Long id);
}
