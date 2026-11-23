package mx.com.liverpool.orders.repository;

import mx.com.liverpool.orders.domain.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {

    Optional<Customer> findByEmailIgnoreCase(String email);

    List<Customer> findAllByOrderByFirstNameAscPaternalLastNameAsc();
}
