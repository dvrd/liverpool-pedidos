package mx.com.liverpool.orders.service;

import mx.com.liverpool.orders.web.dto.CreateCustomerRequest;
import mx.com.liverpool.orders.web.dto.CustomerRecord;
import mx.com.liverpool.orders.web.dto.UpdateCustomerRequest;

import java.util.List;

public interface CustomerService {

    CustomerRecord create(CreateCustomerRequest request);

    List<CustomerRecord> list();

    CustomerRecord get(String id);

    CustomerRecord update(String id, UpdateCustomerRequest request);

    void delete(String id);
}
