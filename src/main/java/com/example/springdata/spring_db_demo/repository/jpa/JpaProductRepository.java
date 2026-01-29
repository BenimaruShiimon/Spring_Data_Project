package com.example.springdata.spring_db_demo.repository.jpa;

import com.example.springdata.spring_db_demo.entity.Product;
import com.example.springdata.spring_db_demo.repository.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("jpa")
public interface JpaProductRepository extends ProductRepository, JpaRepository<Product, Long> {

}
