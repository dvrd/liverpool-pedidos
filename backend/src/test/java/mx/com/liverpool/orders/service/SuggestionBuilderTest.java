package mx.com.liverpool.orders.service;

import mx.com.liverpool.orders.domain.CustomerSnapshot;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.SuggestionType;
import mx.com.liverpool.orders.web.dto.SuggestionResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SuggestionBuilderTest {

    private final SuggestionBuilder builder = new SuggestionBuilder();

    @Test
    void findsByNameWithoutAccentsAndDoesNotRepeatTheCustomer() {
        Order first = order("LIV-000001", "José", "Herrera", "Díaz", "jose.herrera@correo.com", "LP-ELC-77810", "Audífonos");
        Order second = order("LIV-000013", "José", "Herrera", "Díaz", "jose.herrera@correo.com", "LP-HOG-35220", "Cafetera");

        List<SuggestionResponse> suggestions = builder.build("jose", Arrays.asList(first, second));

        assertThat(suggestions).extracting(SuggestionResponse::getType).contains(SuggestionType.CUSTOMER, SuggestionType.EMAIL);
        assertThat(suggestions).filteredOn(item -> item.getType() == SuggestionType.CUSTOMER).hasSize(1);
        assertThat(suggestions).filteredOn(item -> item.getType() == SuggestionType.CUSTOMER)
                .extracting(SuggestionResponse::getValue)
                .containsExactly("José Herrera Díaz");
    }

    @Test
    void shortTermSuggestsNothing() {
        Order order = order("LIV-000001", "Ana", "Ruiz", "Solis", "ana@correo.com", "LP-MOD-1", "Vestido");
        assertThat(builder.build("a", Arrays.asList(order))).isEmpty();
    }

    @Test
    void suggestsTheCodeWhenTheTermIsInTheProduct() {
        Order order = order("LIV-000002", "Diego", "Paredes", "Cruz", "diego@correo.com", "LP-ELC-79015", "Barra de sonido");

        List<SuggestionResponse> suggestions = builder.build("LP-ELC", Arrays.asList(order));

        assertThat(suggestions).extracting(SuggestionResponse::getValue).contains("LP-ELC-79015");
    }

    private Order order(String orderNumber, String firstName, String paternalLastName, String maternalLastName,
                        String email, String productCode, String productDescription) {
        CustomerSnapshot customer = new CustomerSnapshot();
        customer.setFirstName(firstName);
        customer.setPaternalLastName(paternalLastName);
        customer.setMaternalLastName(maternalLastName);
        customer.setEmail(email);
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setProductCode(productCode);
        order.setProductDescription(productDescription);
        return order;
    }
}
