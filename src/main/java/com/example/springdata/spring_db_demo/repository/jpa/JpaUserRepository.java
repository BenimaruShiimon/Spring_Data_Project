package com.example.springdata.spring_db_demo.repository.jpa;

import com.example.springdata.spring_db_demo.entity.User;
import com.example.springdata.spring_db_demo.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Profile("jpa")
public interface JpaUserRepository extends UserRepository, JpaRepository<User, Long> {
    @Override
    @EntityGraph(attributePaths = {"orders", "orders.products"})
    Optional<User> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"orders", "orders.products"})
    List<User> findAll();
}
