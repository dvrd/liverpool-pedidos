package mx.com.liverpool.orders.service.impl;

import mx.com.liverpool.orders.common.Identifiers;
import mx.com.liverpool.orders.common.Money;
import mx.com.liverpool.orders.common.Text;
import mx.com.liverpool.orders.domain.Customer;
import mx.com.liverpool.orders.domain.CustomerSnapshot;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.OrderChannel;
import mx.com.liverpool.orders.domain.OrderStatus;
import mx.com.liverpool.orders.repository.CustomerRepository;
import mx.com.liverpool.orders.repository.OrderRepository;
import mx.com.liverpool.orders.repository.OrderSearch;
import mx.com.liverpool.orders.repository.SearchResult;
import mx.com.liverpool.orders.service.OrderNumberService;
import mx.com.liverpool.orders.service.OrderService;
import mx.com.liverpool.orders.service.SuggestionBuilder;
import mx.com.liverpool.orders.web.dto.CreateOrderRequest;
import mx.com.liverpool.orders.web.dto.OrderResponse;
import mx.com.liverpool.orders.web.dto.PageResponse;
import mx.com.liverpool.orders.web.dto.SuggestionResponse;
import mx.com.liverpool.orders.web.dto.UpdateOrderRequest;
import mx.com.liverpool.orders.web.error.BusinessRuleException;
import mx.com.liverpool.orders.web.error.NotFoundException;
import mx.com.liverpool.orders.web.mapping.OrderMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final OrderSearch search;
    private final OrderNumberService orderNumbers;
    private final SuggestionBuilder suggestions;
    private final OrderMapper mapper;

    public OrderServiceImpl(OrderRepository orders, CustomerRepository customers, OrderSearch search,
                            OrderNumberService orderNumbers, SuggestionBuilder suggestions, OrderMapper mapper) {
        this.orders = orders;
        this.customers = customers;
        this.search = search;
        this.orderNumbers = orderNumbers;
        this.suggestions = suggestions;
        this.mapper = mapper;
    }

    @Override
    public OrderResponse create(CreateOrderRequest request) {
        Customer customer = findCustomer(request.getCustomerId());
        Instant now = Instant.now();
        OrderStatus status = request.getStatus() == null ? OrderStatus.RECEIVED : request.getStatus();

        Order order = new Order();
        order.setOrderNumber(orderNumbers.next());
        order.setCustomerId(customer.getId());
        order.setCustomer(CustomerSnapshot.from(customer));
        applyFields(order, request.getShippingAddress(), request.getProductCode(),
                request.getProductDescription(), request.getQuantity(), request.getUnitPrice(), request.getChannel());
        order.setCreatedAt(now);
        order.recordStatus(status, now);
        order.setSearchText(Text.searchIndex(order));
        return mapper.toResponse(orders.save(order));
    }

    @Override
    public PageResponse<OrderResponse> list(String term, String status, int page, int size) {
        if (page < 0) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "La página no puede ser negativa.");
        }
        if (size < 1 || size > 100) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "El tamaño de página debe estar entre 1 y 100.");
        }
        SearchResult result = search.search(term, parseStatus(status), page, size);
        List<OrderResponse> content = new ArrayList<OrderResponse>();
        for (Order order : result.getOrders()) {
            content.add(mapper.toResponse(order));
        }
        return PageResponse.of(content, page, size, result.getTotal());
    }

    @Override
    public OrderResponse get(String id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public OrderResponse update(String id, UpdateOrderRequest request) {
        Order order = find(id);
        applyFields(order, request.getShippingAddress(), request.getProductCode(),
                request.getProductDescription(), request.getQuantity(), request.getUnitPrice(), request.getChannel());
        if (order.getStatus() != request.getStatus()) {
            order.recordStatus(request.getStatus(), Instant.now());
        } else {
            order.setUpdatedAt(Instant.now());
        }
        order.setSearchText(Text.searchIndex(order));
        return mapper.toResponse(orders.save(order));
    }

    @Override
    public OrderResponse changeStatus(String id, OrderStatus status) {
        Order order = find(id);
        if (order.getStatus() != status) {
            order.recordStatus(status, Instant.now());
            order.setSearchText(Text.searchIndex(order));
            order = orders.save(order);
        }
        return mapper.toResponse(order);
    }

    @Override
    public void delete(String id) {
        Order order = find(id);
        orders.deleteById(order.getId());
    }

    @Override
    public List<SuggestionResponse> suggest(String term) {
        if (Text.normalize(term).length() < 2) {
            return suggestions.build(term, new ArrayList<Order>());
        }
        return suggestions.build(term, search.matches(term, 15));
    }

    private void applyFields(Order order, String address, String code, String description,
                             int quantity, BigDecimal price, OrderChannel channel) {
        order.setShippingAddress(Text.clean(address));
        order.setProductCode(Text.clean(code).toUpperCase(Locale.ROOT));
        order.setProductDescription(Text.clean(description));
        order.setQuantity(quantity);
        order.setUnitPrice(Money.amount(price));
        order.setChannel(channel);
    }

    private Order find(String id) {
        Identifiers.require(id, "Pedido no encontrado.");
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Pedido no encontrado."));
    }

    private Customer findCustomer(String id) {
        Identifiers.require(id, "Cliente no encontrado.");
        return customers.findById(id).orElseThrow(() -> new NotFoundException("Cliente no encontrado."));
    }

    private OrderStatus parseStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Estatus no reconocido.");
        }
    }
}
