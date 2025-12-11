package com.example.springdata.spring_db_demo.entity.repository.jdbs;

import com.example.springdata.spring_db_demo.entity.Order;
import com.example.springdata.spring_db_demo.entity.Product;
import com.example.springdata.spring_db_demo.entity.User;
import com.example.springdata.spring_db_demo.entity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Profile("jdbc")
@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<User> findById(Long userId) {
        String sql = "SELECT u.id AS user_id, u.name AS user_name, "
                + "o.id AS order_id, o.price AS order_price, "
                + "p.id AS product_id, p.name AS product_name, p.price AS product_price "
                + "FROM usr u "
                + "LEFT JOIN orders o ON u.id = o.user_id "
                + "LEFT JOIN order_product op ON o.id = op.order_id "
                + "LEFT JOIN products p ON op.product_id = p.id "
                + "WHERE u.id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserExtractor(), userId);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.getFirst());
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT u.id AS user_id, u.name AS user_name, "
                + "o.id AS order_id, o.price AS order_price, "
                + "p.id AS product_id, p.name AS product_name, p.price AS product_price "
                + "FROM usr u "
                + "LEFT JOIN orders o ON u.id = o.user_id "
                + "LEFT JOIN order_product op ON o.id = op.order_id "
                + "LEFT JOIN products p ON op.product_id = p.id";
        return jdbcTemplate.query(sql, new UserExtractor());
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            String sql = "INSERT INTO usr (name) VALUES (?)";
            jdbcTemplate.update(sql, user.getName());
            user.setId(jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class));
        } else {
            String sql = "UPDATE usr SET name = ? WHERE id = ?";
            jdbcTemplate.update(sql, user.getName(), user.getId());
        }
        for (Order order : user.getOrders()) {
            order.setUser(user);
            saveOrder(order);
        }
        return user;
    }

    @Override
    public void deleteById(Long id) {
        // Удаление заказов и связанной продукции
        String deleteOrderProductsSql = "DELETE FROM order_product WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)";
        jdbcTemplate.update(deleteOrderProductsSql, id);
        String deleteOrdersSql = "DELETE FROM orders WHERE user_id = ?";
        jdbcTemplate.update(deleteOrdersSql, id);
        String deleteUserSql = "DELETE FROM usr WHERE id = ?";
        jdbcTemplate.update(deleteUserSql, id);
    }

    private void saveOrder(Order order) {
        if (order.getId() == null) {
            String sql = "INSERT INTO orders (price, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, order.getPrice(), order.getUser().getId());
            order.setId(jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class));
        } else {
            String sql = "UPDATE orders SET price = ?, user_id = ? WHERE id = ?";
            jdbcTemplate.update(sql, order.getPrice(), order.getUser().getId(), order.getId());

        }
        for (Product product : order.getProducts()) {
            String orderProductSql = "INSERT INTO order_product (order_id, product_id) VALUES (?, ?)";
            jdbcTemplate.update(orderProductSql, order.getId(), product.getId());
        }
    }

    private static class UserExtractor implements ResultSetExtractor<List<User>> {

        @Override
        public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, User> userMap = new HashMap<>();
            Map<Long, Order> orderMap = new HashMap<>();
            Map<Long, Product> productMap = new HashMap<>();

            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                User user = userMap.get(userId);
                if (user == null) {
                    user = new User();
                    user.setId(userId);
                    user.setName(rs.getString("user_name"));
                    user.setOrders(new HashSet<>());
                    userMap.put(userId, user);
                }

                Long orderId = rs.getLong("order_id");
                Order order = null;
                if (orderId != null && orderId != 0) {
                    order = orderMap.get(orderId);
                    if (order == null) {
                        order = new Order();
                        order.setId(orderId);
                        order.setPrice(rs.getBigDecimal("order_price"));
                        order.setUser(user);
                        order.setProducts(new HashSet<>());
                        user.getOrders().add(order);
                        orderMap.put(orderId, order);
                    }
                }

                Long productId = rs.getLong("product_id");
                if (productId != null && productId != 0) {
                    Product product = productMap.get(productId);
                    if (product == null) {
                        product = new Product();
                        product.setId(productId);
                        product.setName(rs.getString("product_name"));
                        product.setPrice(rs.getBigDecimal("product_price"));
                        product.setOrders(new HashSet<>());
                        productMap.put(productId, product);
                    }
                    if (order != null) {
                        product.getOrders().add(order);
                        order.getProducts().add(product);
                    }
                }
            }
            return new ArrayList<>(userMap.values());
        }
    }
}