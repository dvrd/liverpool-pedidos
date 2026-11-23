package mx.com.liverpool.orders.repository;

import mx.com.liverpool.orders.domain.Order;

import java.util.List;

public class SearchResult {

    private final List<Order> orders;
    private final long total;

    public SearchResult(List<Order> orders, long total) {
        this.orders = orders;
        this.total = total;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public long getTotal() {
        return total;
    }
}
