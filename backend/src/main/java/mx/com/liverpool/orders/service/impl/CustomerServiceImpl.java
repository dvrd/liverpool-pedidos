package mx.com.liverpool.orders.service.impl;

import mx.com.liverpool.orders.common.Identifiers;
import mx.com.liverpool.orders.common.Text;
import mx.com.liverpool.orders.domain.Customer;
import mx.com.liverpool.orders.repository.CustomerRepository;
import mx.com.liverpool.orders.repository.OrderRepository;
import mx.com.liverpool.orders.service.CustomerService;
import mx.com.liverpool.orders.web.dto.CreateCustomerRequest;
import mx.com.liverpool.orders.web.dto.CustomerFields;
import mx.com.liverpool.orders.web.dto.CustomerRecord;
import mx.com.liverpool.orders.web.dto.UpdateCustomerRequest;
import mx.com.liverpool.orders.web.error.BusinessRuleException;
import mx.com.liverpool.orders.web.error.NotFoundException;
import mx.com.liverpool.orders.web.mapping.CustomerMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customers;
    private final OrderRepository orders;
    private final CustomerMapper mapper;

    public CustomerServiceImpl(CustomerRepository customers, OrderRepository orders, CustomerMapper mapper) {
        this.customers = customers;
        this.orders = orders;
        this.mapper = mapper;
    }

    @Override
    public CustomerRecord create(CreateCustomerRequest request) {
        requireUniqueEmail(Text.email(request.getEmail()), null);
        Instant now = Instant.now();
        Customer customer = new Customer();
        applyIdentity(customer, request);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        return mapper.toResponse(save(customer));
    }

    @Override
    public List<CustomerRecord> list() {
        List<CustomerRecord> response = new ArrayList<CustomerRecord>();
        for (Customer customer : customers.findAllByOrderByFirstNameAscPaternalLastNameAsc()) {
            response.add(mapper.toResponse(customer));
        }
        return response;
    }

    @Override
    public CustomerRecord get(String id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public CustomerRecord update(String id, UpdateCustomerRequest request) {
        Customer customer = find(id);
        requireUniqueEmail(Text.email(request.getEmail()), customer.getId());
        applyIdentity(customer, request);
        customer.setUpdatedAt(Instant.now());
        return mapper.toResponse(save(customer));
    }

    private void applyIdentity(Customer customer, CustomerFields request) {
        customer.setFirstName(Text.clean(request.getFirstName()));
        customer.setPaternalLastName(Text.clean(request.getPaternalLastName()));
        customer.setMaternalLastName(Text.clean(request.getMaternalLastName()));
        customer.setEmail(Text.email(request.getEmail()));
    }

    private void requireUniqueEmail(String email, String currentId) {
        Optional<Customer> other = customers.findByEmailIgnoreCase(email);
        if (!other.isPresent()) {
            return;
        }
        if (currentId != null && currentId.equals(other.get().getId())) {
            return;
        }
        throw duplicateEmail();
    }

    @Override
    public void delete(String id) {
        Customer customer = find(id);
        if (orders.existsByCustomerId(customer.getId())) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el cliente porque tiene pedidos asociados.");
        }
        customers.deleteById(customer.getId());
    }

    private Customer find(String id) {
        Identifiers.require(id, "Cliente no encontrado.");
        return customers.findById(id).orElseThrow(() -> new NotFoundException("Cliente no encontrado."));
    }

    private Customer save(Customer customer) {
        try {
            return customers.save(customer);
        } catch (DuplicateKeyException ex) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Ya existe un cliente con ese correo electrónico.", ex);
        }
    }

    private BusinessRuleException duplicateEmail() {
        return new BusinessRuleException(HttpStatus.CONFLICT, "Ya existe un cliente con ese correo electrónico.");
    }
}
