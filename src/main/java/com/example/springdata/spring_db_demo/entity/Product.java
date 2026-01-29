package com.example.springdata.spring_db_demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Builder
@SuperBuilder
@RequiredArgsConstructor
//@Table(name = "products") // Сущность не привязана к таблице?
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    @ToString.Exclude
    @ManyToMany(mappedBy = "products")
    private Set<Order> orders = new HashSet<>();
}
