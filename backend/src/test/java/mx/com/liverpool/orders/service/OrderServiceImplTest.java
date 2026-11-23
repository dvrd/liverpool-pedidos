package mx.com.liverpool.orders.service;

import mx.com.liverpool.orders.domain.Customer;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.OrderChannel;
import mx.com.liverpool.orders.domain.OrderStatus;
import mx.com.liverpool.orders.repository.CustomerRepository;
import mx.com.liverpool.orders.repository.OrderRepository;
import mx.com.liverpool.orders.repository.OrderSearch;
import mx.com.liverpool.orders.repository.SearchResult;
import mx.com.liverpool.orders.service.impl.OrderServiceImpl;
import mx.com.liverpool.orders.web.dto.CreateOrderRequest;
import mx.com.liverpool.orders.web.dto.OrderResponse;
import mx.com.liverpool.orders.web.dto.PageResponse;
import mx.com.liverpool.orders.web.dto.UpdateOrderRequest;
import mx.com.liverpool.orders.web.error.BusinessRuleException;
import mx.com.liverpool.orders.web.error.NotFoundException;
import mx.com.liverpool.orders.web.mapping.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orders;
    @Mock
    private CustomerRepository customers;
    @Mock
    private OrderSearch search;
    private static final String ORDER_ID = "507f1f77bcf86cd799439012";

    @Mock
    private OrderNumberService orderNumbers;

    @Test
    void createCopiesTheCustomerAndCalculatesNumberAndTotal() {
        Customer customer = new Customer();
        customer.setId("507f1f77bcf86cd799439011");
        customer.setFirstName("María");
        customer.setPaternalLastName("Núñez");
        customer.setMaternalLastName("Solís");
        customer.setEmail("maria.nunez@correo.com");
        when(customers.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(orderNumbers.next()).thenReturn("LIV-000099");
        when(orders.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId("507f1f77bcf86cd799439012");
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customer.getId());
        request.setShippingAddress("Francisco Sosa 120, Coyoacán");
        request.setProductCode("lp-per-22018");
        request.setProductDescription("Eau de parfum");
        request.setQuantity(2);
        request.setUnitPrice(new BigDecimal("1650"));
        request.setChannel(OrderChannel.WEBSITE);

        OrderServiceImpl service = service();
        OrderResponse response = service.create(request);

        assertThat(response.getOrderNumber()).isEqualTo("LIV-000099");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("3300.00");
        assertThat(response.getCustomer().getEmail()).isEqualTo("maria.nunez@correo.com");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(response.getProductCode()).isEqualTo("LP-PER-22018");
        assertThat(response.getHistory()).hasSize(1);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orders).save(captor.capture());
        assertThat(captor.getValue().getSearchText()).contains("maria nunez");
    }

    @Test
    void createRejectsAnUnknownCustomer() {
        when(customers.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().create(request()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Cliente no encontrado.");
        verify(orderNumbers, never()).next();
    }

    @Test
    void updateRecordsHistoryOnlyWhenTheStatusChanges() {
        Order order = stored(OrderStatus.RECEIVED);
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse updated = service().update(ORDER_ID, correction(OrderStatus.SHIPPED));

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(updated.getHistory()).hasSize(2);
        assertThat(updated.getTotalAmount()).isEqualByComparingTo("200.00");
        assertThat(updated.getProductCode()).isEqualTo("LP-ABC-10001");
        assertThat(order.getSearchText()).contains("enviado");
    }

    @Test
    void updateKeepsHistoryWhenTheStatusStays() {
        stored(OrderStatus.RECEIVED);
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse updated = service().update(ORDER_ID, correction(OrderStatus.RECEIVED));

        assertThat(updated.getHistory()).hasSize(1);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.RECEIVED);
    }

    @Test
    void changeStatusDoesNothingWhenItIsAlreadyThatStatus() {
        stored(OrderStatus.RECEIVED);

        OrderResponse response = service().changeStatus(ORDER_ID, OrderStatus.RECEIVED);

        assertThat(response.getHistory()).hasSize(1);
        verify(orders, never()).save(any());
    }

    @Test
    void changeStatusAppendsTheNewStatus() {
        stored(OrderStatus.RECEIVED);
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service().changeStatus(ORDER_ID, OrderStatus.DELIVERED);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(response.getHistory()).hasSize(2);
    }

    @Test
    void deleteRemovesTheOrder() {
        stored(OrderStatus.CANCELLED);

        service().delete(ORDER_ID);

        verify(orders).deleteById(ORDER_ID);
    }

    @Test
    void missingOrderIsNotFound() {
        when(orders.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().get(ORDER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Pedido no encontrado.");
    }

    @Test
    void listRejectsANegativePage() {
        assertThatThrownBy(() -> service().list(null, null, -1, 20))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La página no puede ser negativa.");
        verify(search, never()).search(any(), any(), anyInt(), anyInt());
    }

    @Test
    void listRejectsASizeOutsideTheRange() {
        assertThatThrownBy(() -> service().list(null, null, 0, 0))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El tamaño de página debe estar entre 1 y 100.");
        assertThatThrownBy(() -> service().list(null, null, 0, 101))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El tamaño de página debe estar entre 1 y 100.");
    }

    @Test
    void listRejectsAnUnknownStatus() {
        assertThatThrownBy(() -> service().list(null, "nope", 0, 20))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Estatus no reconocido.");
    }

    @Test
    void listReturnsTheRequestedPage() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setOrderNumber("LIV-000010");
        order.setQuantity(1);
        order.setUnitPrice(new BigDecimal("50.00"));
        order.setChannel(OrderChannel.APP);
        order.recordStatus(OrderStatus.RECEIVED, Instant.parse("2026-01-01T00:00:00Z"));
        when(search.search("mesa", OrderStatus.RECEIVED, 0, 20))
                .thenReturn(new SearchResult(Collections.singletonList(order), 21));

        PageResponse<OrderResponse> page = service().list("mesa", "received", 0, 20);

        assertThat(page.getTotalElements()).isEqualTo(21);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(OrderResponse::getOrderNumber).containsExactly("LIV-000010");
    }

    @Test
    void suggestionsNeedTwoCharacters() {
        assertThat(service().suggest("a")).isEmpty();
        verify(search, never()).matches(any(), anyInt());
    }

    private Order stored(OrderStatus status) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setOrderNumber("LIV-000010");
        order.setQuantity(1);
        order.setUnitPrice(new BigDecimal("50.00"));
        order.setChannel(OrderChannel.WEBSITE);
        order.setShippingAddress("Calle Vieja 1");
        order.setProductCode("LP-OLD-1000");
        order.setProductDescription("Antes");
        order.recordStatus(status, Instant.parse("2026-01-01T00:00:00Z"));
        when(orders.findById(ORDER_ID)).thenReturn(Optional.of(order));
        return order;
    }

    private CreateOrderRequest request() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("507f1f77bcf86cd799439011");
        request.setShippingAddress("Francisco Sosa 120, Coyoacán");
        request.setProductCode("lp-per-22018");
        request.setProductDescription("Eau de parfum");
        request.setQuantity(2);
        request.setUnitPrice(new BigDecimal("1650"));
        request.setChannel(OrderChannel.WEBSITE);
        return request;
    }

    private UpdateOrderRequest correction(OrderStatus status) {
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setShippingAddress("Nueva 1");
        request.setProductCode("lp-abc-10001");
        request.setProductDescription("Bufanda");
        request.setQuantity(2);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setChannel(OrderChannel.APP);
        request.setStatus(status);
        return request;
    }

    private OrderServiceImpl service() {
        return new OrderServiceImpl(orders, customers, search, orderNumbers, new SuggestionBuilder(), new OrderMapper());
    }
}
