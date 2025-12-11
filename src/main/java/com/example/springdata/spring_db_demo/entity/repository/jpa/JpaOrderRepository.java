package com.example.springdata.spring_db_demo.entity.repository.jpa;

import com.example.springdata.spring_db_demo.entity.Order;
import com.example.springdata.spring_db_demo.entity.repository.OrderRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("jpa")
public interface JpaOrderRepository extends OrderRepository, JpaRepository<Order, Long> {
}
