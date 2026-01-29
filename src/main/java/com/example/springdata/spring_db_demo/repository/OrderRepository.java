package com.example.springdata.spring_db_demo.repository;

import com.example.springdata.spring_db_demo.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);

    List<Order> findAll();

    List<Order> findByUserId(Long id);

    Order save(Order order);

    void deleteById(Long id);
}
