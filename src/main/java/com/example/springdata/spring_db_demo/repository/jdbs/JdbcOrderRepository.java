package com.example.springdata.spring_db_demo.repository.jdbs;

import com.example.springdata.spring_db_demo.entity.Order;
import com.example.springdata.spring_db_demo.entity.Product;
import com.example.springdata.spring_db_demo.entity.User;
import com.example.springdata.spring_db_demo.repository.OrderRepository;
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
public class JdbcOrderRepository implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Order> findById(Long orderId) {
        // Выглядит страшно =)
        String queryOrder = "SELECT o.id AS order_id, o.price AS order_price, "
                + "u.id AS user_id, u.name AS user_name, "
                + "p.id AS product_id, p.name AS product_name, p.price AS product_price "
                + "FROM orders o "
                + "LEFT JOIN usr u ON o.user_id = u.id "
                + "LEFT JOIN order_product op ON o.id = op.order_id "
                + "LEFT JOIN products p ON op.product_id = p.id "
                + "WHERE o.id = ?";
        List<Order> orders = jdbcTemplate.query(queryOrder, new OrderExtractor(), orderId);
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.getFirst());
    }

    @Override
    public List<Order> findAll() {
        String sqlOrder = "SELECT o.id AS order_id, o.price AS order_price, "
                + "u.id AS user_id, u.name AS user_name, "
                + "p.id AS product_id, p.name AS product_name, p.price AS product_price "
                + "FROM orders o "
                + "LEFT JOIN usr u ON o.user_id = u.id "
                + "LEFT JOIN order_product op ON o.id = op.order_id "
                + "LEFT JOIN products p ON op.product_id = p.id";
        return jdbcTemplate.query(sqlOrder, new OrderExtractor());
    }

    @Override
    public List<Order> findByUserId(Long id) {
        String sqlOrder = "SELECT o.id AS order_id, o.price AS order_price, "
                + "u.id AS user_id, u.name AS user_name, "
                + "p.id AS product_id, p.name AS product_name, p.price AS product_price "
                + "FROM orders o "
                + "LEFT JOIN usr u ON o.user_id = u.id "
                + "LEFT JOIN order_product op ON o.id = op.order_id "
                + "LEFT JOIN products p ON op.product_id = p.id "
                + "WHERE u.id = ?";
        return jdbcTemplate.query(sqlOrder, new OrderExtractor(), id);
    }

    // Я бы в методах, который меняют состояние БД тоже добавил бы @Transactional. В текущей реализации это не критично
    // может быть, но если ты метод переиспользуешь в другом месте, то когда появится ошибка, у тебя может быть
    // не консистентное состояние БД.
    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            // TODO Вставка нового заказа
            String sqlInsert = "INSERT INTO orders (price, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sqlInsert, order.getPrice(), order.getUser().getId());
            // TODO Получение id последней вставленной записи
            Long newId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
            order.setId(newId);
        } else {
            // TODO Обновление существующего заказа
            String sqlUpdate = "UPDATE orders SET price = ?, user_id = ? WHERE id = ?";
            jdbcTemplate.update(sqlUpdate, order.getPrice(), order.getUser().getId(), order.getId());
            // TODO Очистка старых связей продуктов
            String deleteLinks = "DELETE FROM order_product WHERE order_id = ?";
            jdbcTemplate.update(deleteLinks, order.getId());
        }

        // TODO Вставка связей продуктов
        for (Product product : order.getProducts()) {
            String insertLink = "INSERT INTO order_product (order_id, product_id) VALUES (?, ?)";
            jdbcTemplate.update(insertLink, order.getId(), product.getId());
        }
        return order;
    }

    @Override
    public void deleteById(Long id) {
        // TODO Удаление связей
        String deleteLinks = "DELETE FROM order_product WHERE order_id = ?";
        jdbcTemplate.update(deleteLinks, id);
        // TODO Удаление заказа
        String deleteOrder = "DELETE FROM orders WHERE id = ?";
        jdbcTemplate.update(deleteOrder, id);
    }

    private static class OrderExtractor implements ResultSetExtractor<List<Order>> {
        @Override
        public List<Order> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Order> orderMap = new HashMap<>();
            Map<Long, User> userMap = new HashMap<>();
            Map<Long, Product> productMap = new HashMap<>();

            while (rs.next()) {
                Long orderId = rs.getLong("order_id");
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
                    product.getOrders().add(order);
                    order.getProducts().add(product);
                }
            }
            return new ArrayList<>(orderMap.values());
        }
    }
}