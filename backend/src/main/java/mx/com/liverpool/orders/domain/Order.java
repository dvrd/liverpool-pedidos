package mx.com.liverpool.orders.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Indexed(unique = true)
    private String orderNumber;

    @Indexed
    private String customerId;

    private CustomerSnapshot customer;
    private String shippingAddress;

    @Indexed
    private String productCode;

    private String productDescription;
    private int quantity;
    private BigDecimal unitPrice;
    private OrderChannel channel;

    @Indexed
    private OrderStatus status;

    private List<StatusEvent> history = new ArrayList<StatusEvent>();

    /**
     * Lowercase, accent-free copy rebuilt on every write so search and the
     * typeahead share one criterion.
     */
    private String searchText;

    private Instant createdAt;
    private Instant updatedAt;

    public void recordStatus(OrderStatus next, Instant when) {
        if (next == null || when == null) {
            throw new IllegalArgumentException("A status change needs a status and a time.");
        }
        if (history == null) {
            history = new ArrayList<StatusEvent>();
        }
        history.add(StatusEvent.of(next, when));
        this.status = next;
        this.updatedAt = when;
    }
}
