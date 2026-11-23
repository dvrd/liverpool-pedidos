package mx.com.liverpool.orders.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class StatusEvent {

    private OrderStatus status;
    private Instant occurredAt;

    public static StatusEvent of(OrderStatus status, Instant occurredAt) {
        StatusEvent event = new StatusEvent();
        event.setStatus(status);
        event.setOccurredAt(occurredAt);
        return event;
    }
}
