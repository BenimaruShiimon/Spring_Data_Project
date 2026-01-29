package com.example.springdata.spring_db_demo.repository.jpa;

import com.example.springdata.spring_db_demo.entity.Order;
import com.example.springdata.spring_db_demo.repository.OrderRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

// Как будто бы здесь тоже нужен @Repository
@Profile("jpa")
public interface JpaOrderRepository extends OrderRepository, JpaRepository<Order, Long> {
}
