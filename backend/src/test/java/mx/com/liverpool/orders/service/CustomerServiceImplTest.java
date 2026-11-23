package mx.com.liverpool.orders.service;

import mx.com.liverpool.orders.domain.Customer;
import mx.com.liverpool.orders.repository.CustomerRepository;
import mx.com.liverpool.orders.repository.OrderRepository;
import mx.com.liverpool.orders.service.impl.CustomerServiceImpl;
import mx.com.liverpool.orders.web.dto.CreateCustomerRequest;
import mx.com.liverpool.orders.web.dto.CustomerRecord;
import mx.com.liverpool.orders.web.dto.UpdateCustomerRequest;
import mx.com.liverpool.orders.web.error.BusinessRuleException;
import mx.com.liverpool.orders.web.error.NotFoundException;
import mx.com.liverpool.orders.web.mapping.CustomerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    private static final String ID = "507f1f77bcf86cd799439011";

    @Mock
    private CustomerRepository customers;
    @Mock
    private OrderRepository orders;

    @Test
    void storesTheEmailInLowerCase() {
        when(customers.findByEmailIgnoreCase("ana.lopez@correo.com")).thenReturn(Optional.empty());
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(ID);
            return saved;
        });

        CustomerRecord created = service().create(customer("  Ana.Lopez@correo.com "));

        assertThat(created.getEmail()).isEqualTo("ana.lopez@correo.com");
        assertThat(created.getFullName()).isEqualTo("Ana López Díaz");
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customers).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ana.lopez@correo.com");
    }

    @Test
    void rejectsADuplicateEmail() {
        Customer existing = new Customer();
        existing.setId("507f1f77bcf86cd799439099");
        when(customers.findByEmailIgnoreCase("ana.lopez@correo.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service().create(customer("ana.lopez@correo.com")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Ya existe un cliente con ese correo electrónico.")
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
        verify(customers, never()).save(any());
    }

    @Test
    void turnsAConcurrentDuplicateIntoTheSameConflict() {
        when(customers.findByEmailIgnoreCase("ana.lopez@correo.com")).thenReturn(Optional.empty());
        when(customers.save(any(Customer.class))).thenThrow(new DuplicateKeyException("E11000"));

        assertThatThrownBy(() -> service().create(customer("ana.lopez@correo.com")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Ya existe un cliente con ese correo electrónico.");
    }

    @Test
    void keepsTheSameEmailWhenTheCustomerIsUpdated() {
        Customer customer = person();
        when(customers.findById(ID)).thenReturn(Optional.of(customer));
        when(customers.findByEmailIgnoreCase("ana.lopez@correo.com")).thenReturn(Optional.of(customer));
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerRequest request = update("Ana María");
        CustomerRecord updated = service().update(ID, request);

        assertThat(updated.getFirstName()).isEqualTo("Ana María");
        assertThat(updated.getEmail()).isEqualTo("ana.lopez@correo.com");
    }

    @Test
    void rejectsAnEmailThatBelongsToSomeoneElse() {
        Customer customer = person();
        Customer other = person();
        other.setId("507f1f77bcf86cd799439099");
        when(customers.findById(ID)).thenReturn(Optional.of(customer));
        when(customers.findByEmailIgnoreCase("ana.lopez@correo.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service().update(ID, update("Ana")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("correo electrónico");
        verify(customers, never()).save(any());
    }

    @Test
    void deletesACustomerWhoHasNoOrders() {
        when(customers.findById(ID)).thenReturn(Optional.of(person()));
        when(orders.existsByCustomerId(ID)).thenReturn(false);

        service().delete(ID);

        verify(customers).deleteById(ID);
    }

    @Test
    void doesNotDeleteACustomerWhoHasOrders() {
        when(customers.findById(ID)).thenReturn(Optional.of(person()));
        when(orders.existsByCustomerId(ID)).thenReturn(true);

        assertThatThrownBy(() -> service().delete(ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No se puede eliminar el cliente porque tiene pedidos asociados.");
        verify(customers, never()).deleteById(any());
    }

    @Test
    void missingCustomerIsNotFound() {
        when(customers.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().get(ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Cliente no encontrado.");
    }

    @Test
    void aBlankIdDoesNotTouchTheDatabase() {
        assertThatThrownBy(() -> service().get("no"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Cliente no encontrado.");
        verify(customers, never()).findById(any());
    }

    @Test
    void listsCustomersByTheStoredName() {
        when(customers.findAllByOrderByFirstNameAscPaternalLastNameAsc())
                .thenReturn(Collections.singletonList(person()));

        assertThat(service().list()).extracting(CustomerRecord::getFullName).containsExactly("Ana López Díaz");
    }

    private CustomerServiceImpl service() {
        return new CustomerServiceImpl(customers, orders, new CustomerMapper());
    }

    private Customer person() {
        Customer customer = new Customer();
        customer.setId(ID);
        customer.setFirstName("Ana");
        customer.setPaternalLastName("López");
        customer.setMaternalLastName("Díaz");
        customer.setEmail("ana.lopez@correo.com");
        return customer;
    }

    private CreateCustomerRequest customer(String email) {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Ana");
        request.setPaternalLastName("López");
        request.setMaternalLastName("Díaz");
        request.setEmail(email);
        return request;
    }

    private UpdateCustomerRequest update(String firstName) {
        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setFirstName(firstName);
        request.setPaternalLastName("López");
        request.setMaternalLastName("Díaz");
        request.setEmail("ana.lopez@correo.com");
        return request;
    }
}
