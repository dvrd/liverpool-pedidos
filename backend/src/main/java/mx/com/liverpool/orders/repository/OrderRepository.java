package mx.com.liverpool.orders.repository;

import mx.com.liverpool.orders.domain.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {

    boolean existsByCustomerId(String customerId);
}
