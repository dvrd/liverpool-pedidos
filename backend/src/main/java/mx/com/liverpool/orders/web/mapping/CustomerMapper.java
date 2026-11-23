package mx.com.liverpool.orders.web.mapping;

import mx.com.liverpool.orders.domain.Customer;
import mx.com.liverpool.orders.web.dto.CustomerRecord;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerRecord toResponse(Customer customer) {
        CustomerRecord response = new CustomerRecord();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setPaternalLastName(customer.getPaternalLastName());
        response.setMaternalLastName(customer.getMaternalLastName());
        response.setFullName(customer.fullName());
        response.setEmail(customer.getEmail());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }
}
