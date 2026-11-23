package mx.com.liverpool.orders.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void recordStatusAppendsHistoryAndMovesTheClock() {
        Order order = new Order();
        Instant when = Instant.parse("2026-03-01T15:04:00Z");

        order.recordStatus(OrderStatus.PREPARING, when);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(order.getUpdatedAt()).isEqualTo(when);
        assertThat(order.getHistory()).hasSize(1);
        assertThat(order.getHistory().get(0).getStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(order.getHistory().get(0).getOccurredAt()).isEqualTo(when);
    }

    @Test
    void recordStatusRefusesAMissingStatusOrTime() {
        Order order = new Order();
        Instant when = Instant.parse("2026-03-01T15:04:00Z");

        assertThatThrownBy(() -> order.recordStatus(null, when))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.recordStatus(OrderStatus.SHIPPED, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(order.getStatus()).isNull();
        assertThat(order.getHistory()).isEmpty();
    }
}
