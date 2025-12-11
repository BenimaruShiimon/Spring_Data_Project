package com.example.springdata;

import com.example.springdata.spring_db_demo.entity.Order;
import com.example.springdata.spring_db_demo.entity.Product;
import com.example.springdata.spring_db_demo.entity.User;
import com.example.springdata.spring_db_demo.entity.repository.jdbs.JdbcOrderRepository;
import com.example.springdata.spring_db_demo.entity.repository.jdbs.JdbcProductRepository;
import com.example.springdata.spring_db_demo.entity.repository.jdbs.JdbcUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class SpringDataApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringDataApplication.class);

    @Autowired
    private JdbcOrderRepository jdbcOrderRepository;

    @Autowired
    private JdbcProductRepository jdbcProductRepository;

    @Autowired
    private JdbcUserRepository jdbcUserRepository;

    @Autowired
    private EntityManager entityManager;

    public static void main(String[] args) {
        SpringApplication.run(SpringDataApplication.class, args);
    }
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent applicationReadyEvent) {
        User user = new User();
        user.setName("Jonathan");
        User user2 = new User();
        user2.setName("John Doe");

        jdbcUserRepository.save(user);
        jdbcUserRepository.save(user2);

        log.info("------------");
        log.info("User 1 successfully created and saved");
        log.info("User1: {} ", jdbcUserRepository.findById(user.getId()));
        log.info("------------");
        log.info("User 2 successfully created and saved");
        log.info("User2: {} ", jdbcUserRepository.findById(user2.getId()));
        log.info("------------");
        log.info("All Users : {}", jdbcUserRepository.findAll());

        Product product = new Product();
        product.setName("Product A");
        product.setPrice(new BigDecimal(2.33));
        Product product2 = new Product();
        product2.setName("Product B");
        product2.setPrice(new BigDecimal(10.22));
        Product product3 = new Product();
        product3.setName("Product C");
        product3.setPrice(new BigDecimal(15.99));

        jdbcProductRepository.save(product);
        jdbcProductRepository.save(product2);
        jdbcProductRepository.save(product3);
        log.info("------------");
        log.info("Product A successfully created and saved");
        log.info("Product A: {}", jdbcProductRepository.findById(product.getId()));
        log.info("------------");
        log.info("Product B successfully created and saved");
        log.info("Product B: {}", jdbcProductRepository.findById(product2.getId()));
        log.info("------------");
        log.info("Product B successfully created and saved");
        log.info("Product C: {}", jdbcProductRepository.findById(product3.getId()));
        log.info("------------");
        log.info("First and Second product is: {}", jdbcProductRepository.findAll(
                Pageable.ofSize(2)
        ).getContent());
        log.info("------------");

        Order order = new Order();
        order.setUser(user);
        Set<Product> products = new HashSet<>();
        products.add(product);
        products.add(product2);
        order.setProducts(products);
        order.setPrice(order.getProducts().stream().map(p -> p.getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add));

        Order order2 = new Order();
        order2.setUser(user2);
        Set<Product> products2 = new HashSet<>();
        products2.add(product3);
        order2.setProducts(products2);
        order2.setPrice(order2.getProducts().stream().map(p -> p.getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add));

        jdbcOrderRepository.save(order);
        jdbcOrderRepository.save(order2);

        log.info("------------");
        log.info("All user after save orders: {}", jdbcOrderRepository.findAll());
        log.info("------------");

        log.info("Order: {}", jdbcOrderRepository.findById(order.getId()));
        log.info("Order2: {}", jdbcOrderRepository.findById(order2.getId()));
        log.info("------------");

        log.info("All orders by user: {}", jdbcOrderRepository.findByUserId(user.getId()));
        log.info("------------");
        log.info("All orders by user2: {}", jdbcOrderRepository.findByUserId(user2.getId()));
        log.info("All orders: {}", jdbcOrderRepository.findAll());
        log.info("------------");

        entityManager.clear();
        log.info("Delete order by id: {}", jdbcOrderRepository.findById(order.getId()));
        jdbcOrderRepository.deleteById(order.getId());
        log.info("-----------");
        log.info("All orders after delete {}", jdbcOrderRepository.findAll());
        log.info("------------");

        entityManager.clear();
        log.info("Delete User by Id: {}",  user2);
        jdbcUserRepository.deleteById(user2.getId());
        log.info("------------");
        log.info("Orders after delete user2 {}", jdbcOrderRepository.findAll());
        log.info("All users after delete {}", jdbcUserRepository.findAll());
        log.info("------------");

        log.info("Test data inserted successfully");
    }

    public void exceptionMethod(){
        throw new RuntimeException("Test Exception");
    }
}
