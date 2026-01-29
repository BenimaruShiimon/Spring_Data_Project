package com.example.springdata.spring_db_demo.repository;

import com.example.springdata.spring_db_demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);

    // Page удобен, когда тебе нужно отдать на фронт, но думаю, здесь было бы уместнее просто List
    Page<Product> findAll(Pageable pageable);

    Product save(Product product);
}
