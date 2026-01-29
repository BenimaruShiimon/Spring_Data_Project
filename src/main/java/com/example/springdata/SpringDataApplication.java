package com.example.springdata;

import com.example.springdata.spring_db_demo.entity.Order;
import com.example.springdata.spring_db_demo.entity.Product;
import com.example.springdata.spring_db_demo.entity.User;
import com.example.springdata.spring_db_demo.repository.OrderRepository;
import com.example.springdata.spring_db_demo.repository.ProductRepository;
import com.example.springdata.spring_db_demo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@SpringBootApplication
public class SpringDataApplication {
    // Не до конца понял, что ты хотел разделением jpa и jdbc, но на jpa всё выглядит намного проще и лучше выбрать либо
    // одно, либо другое. Если оставишь так,
    // как у меня, то всё, что связано с jdbc можно удалить.
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    public static void main(String[] args) {
        SpringApplication.run(SpringDataApplication.class, args);
    }

    // Лучше вынести в отдельный компонент
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent applicationReadyEvent) {
        User user = User.builder().name("Jonathan").build();
        User user2 = User.builder().name("John Doe").build();

        userRepository.save(user);
        userRepository.save(user2);

        log.info("------------");
        log.info("User 1 successfully created and saved");
        log.info("User1: {} ", userRepository.findById(user.getId()));
        log.info("------------");
        log.info("User 2 successfully created and saved");
        log.info("User2: {} ", userRepository.findById(user2.getId()));
        log.info("------------");
        log.info("All Users : {}", userRepository.findAll());

        Product product = Product.builder().name("Product A").price(BigDecimal.valueOf(2.33)).build();
        Product product2 = Product.builder().name("Product B").price(BigDecimal.valueOf(10.22)).build();
        Product product3 = Product.builder().name("Product C").price(BigDecimal.valueOf(15.99)).build();

        // Тут лучше сохранить всё скопом, что-то вроде productRepository.saveAll(products);
        productRepository.save(product);
        productRepository.save(product2);
        productRepository.save(product3);

        // На каждый элемент дёргать запросом БД дороговато. Сейчас это не сильно критично, но на больших объемах данных
        // можно почувствовать. Лучше сразу дёрнуть всё и логировать в цикле, например.

        log.info("------------");
        log.info("Product A successfully created and saved");
        log.info("Product A: {}", productRepository.findById(product.getId()));
        log.info("------------");
        log.info("Product B successfully created and saved");
        log.info("Product B: {}", productRepository.findById(product2.getId()));
        log.info("------------");
        log.info("Product C successfully created and saved");
        log.info("Product C: {}", productRepository.findById(product3.getId()));
        log.info("------------");
        log.info("First and Second product is: {}", productRepository.findAll(
                Pageable.ofSize(2)
        ).getContent());
        log.info("------------");

        Order order = new Order();
        order.setUser(user);
        Set<Product> products = new HashSet<>();
        products.add(product);
        products.add(product2);
        order.setProducts(products);
        order.setPrice(order.getProducts().stream().map(Product::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));

        Order order2 = new Order();
        order2.setUser(user2);
        Set<Product> products2 = new HashSet<>();
        products2.add(product3);
        order2.setProducts(products2);
        order2.setPrice(order2.getProducts().stream().map(Product::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));

        orderRepository.save(order);
        orderRepository.save(order2);

        log.info("------------");
        log.info("All user after save orders: {}", orderRepository.findAll());
        log.info("------------");

        log.info("Order: {}", orderRepository.findById(order.getId()));
        log.info("Order2: {}", orderRepository.findById(order2.getId()));
        log.info("------------");

        log.info("All orders by user: {}", orderRepository.findByUserId(user.getId()));
        log.info("------------");
        log.info("All orders by user2: {}", orderRepository.findByUserId(user2.getId()));
        log.info("All orders: {}", orderRepository.findAll());
        log.info("------------");

        entityManager.clear(); // Без clear разве не работает? Ни jdbc, ни jpa?
        log.info("Delete order by id: {}", orderRepository.findById(order.getId()));
        orderRepository.deleteById(order.getId());
        log.info("-----------");
        log.info("All orders after delete {}", orderRepository.findAll());
        log.info("------------");

        entityManager.clear();
        log.info("Delete User by Id: {}",  user2);
        userRepository.deleteById(user2.getId());
        log.info("------------");
        log.info("Orders after delete user2 {}", orderRepository.findAll());
        log.info("All users after delete {}", userRepository.findAll());
        log.info("------------");

        log.info("Test data inserted successfully");
    }

    public void exceptionMethod(){
        throw new RuntimeException("Test Exception");
    }
}
