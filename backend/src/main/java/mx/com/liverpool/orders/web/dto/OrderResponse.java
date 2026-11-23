package mx.com.liverpool.orders.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.orders.domain.OrderChannel;
import mx.com.liverpool.orders.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {

    private String id;
    private String orderNumber;
    private OrderChannel channel;
    private String customerId;
    private CustomerResponse customer;
    private String shippingAddress;
    private String productCode;
    private String productDescription;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<StatusEventResponse> history = new ArrayList<StatusEventResponse>();
    private Instant createdAt;
    private Instant updatedAt;
}
