package mx.com.liverpool.orders.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.orders.domain.OrderStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class StatusEventResponse {

    private OrderStatus status;
    private Instant occurredAt;
}
