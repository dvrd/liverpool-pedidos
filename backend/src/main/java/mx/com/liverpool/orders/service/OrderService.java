package mx.com.liverpool.orders.service;

import mx.com.liverpool.orders.domain.OrderStatus;
import mx.com.liverpool.orders.web.dto.CreateOrderRequest;
import mx.com.liverpool.orders.web.dto.OrderResponse;
import mx.com.liverpool.orders.web.dto.PageResponse;
import mx.com.liverpool.orders.web.dto.SuggestionResponse;
import mx.com.liverpool.orders.web.dto.UpdateOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse create(CreateOrderRequest request);

    PageResponse<OrderResponse> list(String term, String status, int page, int size);

    OrderResponse get(String id);

    OrderResponse update(String id, UpdateOrderRequest request);

    OrderResponse changeStatus(String id, OrderStatus status);

    void delete(String id);

    List<SuggestionResponse> suggest(String term);
}
