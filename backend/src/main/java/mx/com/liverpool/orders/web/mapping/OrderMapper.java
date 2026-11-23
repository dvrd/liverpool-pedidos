package mx.com.liverpool.orders.web.mapping;

import mx.com.liverpool.orders.common.Money;
import mx.com.liverpool.orders.domain.CustomerSnapshot;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.StatusEvent;
import mx.com.liverpool.orders.web.dto.CustomerResponse;
import mx.com.liverpool.orders.web.dto.OrderResponse;
import mx.com.liverpool.orders.web.dto.StatusEventResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setChannel(order.getChannel());
        response.setCustomerId(order.getCustomerId());
        response.setCustomer(toCustomer(order.getCustomer()));
        response.setShippingAddress(order.getShippingAddress());
        response.setProductCode(order.getProductCode());
        response.setProductDescription(order.getProductDescription());
        response.setQuantity(order.getQuantity());
        response.setUnitPrice(order.getUnitPrice());
        response.setTotalAmount(Money.total(order.getUnitPrice(), order.getQuantity()));
        response.setStatus(order.getStatus());
        response.setHistory(toHistory(order.getHistory()));
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }

    private CustomerResponse toCustomer(CustomerSnapshot customer) {
        if (customer == null) {
            return null;
        }
        CustomerResponse response = new CustomerResponse();
        response.setFirstName(customer.getFirstName());
        response.setPaternalLastName(customer.getPaternalLastName());
        response.setMaternalLastName(customer.getMaternalLastName());
        response.setFullName(customer.fullName());
        response.setEmail(customer.getEmail());
        return response;
    }

    private List<StatusEventResponse> toHistory(List<StatusEvent> history) {
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }
        List<StatusEventResponse> response = new ArrayList<StatusEventResponse>();
        for (StatusEvent event : history) {
            StatusEventResponse item = new StatusEventResponse();
            item.setStatus(event.getStatus());
            item.setOccurredAt(event.getOccurredAt());
            response.add(item);
        }
        return response;
    }
}
