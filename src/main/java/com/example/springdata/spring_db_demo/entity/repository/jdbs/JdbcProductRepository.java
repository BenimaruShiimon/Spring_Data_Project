package com.example.springdata.spring_db_demo.entity.repository.jdbs;

import com.example.springdata.spring_db_demo.entity.Order;
import com.example.springdata.spring_db_demo.entity.Product;
import com.example.springdata.spring_db_demo.entity.User;
import com.example.springdata.spring_db_demo.entity.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Profile("jdbc")
@Repository
@RequiredArgsConstructor
public class JdbcProductRepository implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Product> findById(Long productId) {
        String query = "SELECT p.id AS product_id, p.name AS product_name, p.price AS product_price, "
                + "o.id AS order_id, o.price AS order_price, "
                + "u.id AS user_id, u.name AS user_name "
                + "FROM products p "
                + "LEFT JOIN order_product op ON p.id = op.product_id "
                + "LEFT JOIN orders o ON op.order_id = o.id "
                + "LEFT JOIN usr u ON o.user_id = u.id "
                + "WHERE p.id = ?";
        List<Product> products = jdbcTemplate.query(query, new ProductExtractor(), productId);
        return products.isEmpty() ? Optional.empty() : Optional.of(products.get(0));
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        String query = "SELECT p.id AS product_id, p.name AS product_name, p.price AS product_price, "
                + "o.id AS order_id, o.price AS order_price, "
                + "u.id AS user_id, u.name AS user_name "
                + "FROM products p "
                + "LEFT JOIN order_product op ON p.id = op.product_id "
                + "LEFT JOIN orders o ON op.order_id = o.id "
                + "LEFT JOIN usr u ON o.user_id = u.id";

        List<Product> products = jdbcTemplate.query(query, new ProductExtractor());

        // TODO Для полноты реализации можно сделать подсчет общего количества, если потребуется
        return new PageImpl<>(products, pageable, products.size());
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            String sql = "INSERT INTO products (name, price) VALUES (?, ?)";
            jdbcTemplate.update(sql, product.getName(), product.getPrice());
            product.setId(jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class));
        } else {
            String sql = "UPDATE products SET name = ?, price = ? WHERE id = ?";
            jdbcTemplate.update(sql, product.getName(), product.getPrice(), product.getId());
        }
        return product;
    }

    private static class ProductExtractor implements ResultSetExtractor<List<Product>> {
        @Override
        public List<Product> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Product> productMap = new HashMap<>();
            Map<Long, Order> orderMap = new HashMap<>();
            Map<Long, User> userMap = new HashMap<>();

            while (rs.next()) {
                Long productId = rs.getLong("product_id");
                Product product = productMap.get(productId);
                if (product == null) {
                    product = new Product();
                    product.setId(productId);
                    product.setName(rs.getString("product_name"));
                    product.setOrders(new HashSet<>());
                    productMap.put(productId, product);
                }

                Long orderId = rs.getLong("order_id");
                if (orderId != null && orderId != 0) {
                    Order order = orderMap.get(orderId);
                    if (order == null) {
                        order = new Order();
                        order.setId(orderId);
                        order.setPrice(rs.getBigDecimal("order_price"));
                        order.setProducts(new HashSet<>());
                        orderMap.put(orderId, order);
                    }

                    Long userId = rs.getLong("user_id");
                    if (userId != null && userId != 0) {
                        User user = userMap.get(userId);
                        if (user == null) {
                            user = new User();
                            user.setId(userId);
                            user.setName(rs.getString("user_name"));
                            user.setOrders(new HashSet<>());
                            userMap.put(userId, user);
                        }
                        order.setUser(user);
                        user.getOrders().add(order);
                    }
                    // Связь продукта с заказом
                    product.getOrders().add(order);
                    order.getProducts().add(product);
                }
            }
            return new ArrayList<>(productMap.values());
        }
    }
}