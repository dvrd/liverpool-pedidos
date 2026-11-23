package mx.com.liverpool.orders.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.com.liverpool.orders.service.OrderService;
import mx.com.liverpool.orders.web.dto.ChangeStatusRequest;
import mx.com.liverpool.orders.web.dto.CreateOrderRequest;
import mx.com.liverpool.orders.web.dto.OrderResponse;
import mx.com.liverpool.orders.web.dto.PageResponse;
import mx.com.liverpool.orders.web.dto.SuggestionResponse;
import mx.com.liverpool.orders.web.dto.UpdateOrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos", description = "Pedidos originados en la aplicación y en el sitio web")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @Operation(summary = "Crea un pedido")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orders.create(request);
        return ResponseEntity.created(ResourceLocation.forId(created.getId())).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista pedidos, con filtro por término y estatus")
    public PageResponse<OrderResponse> list(
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return orders.list(term, status, page, size);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Opciones del typeahead a partir de dos caracteres")
    public List<SuggestionResponse> suggestions(@RequestParam(value = "term", required = false) String term) {
        return orders.suggest(term);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta el detalle de un pedido")
    public OrderResponse get(@PathVariable String id) {
        return orders.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplaza los datos operativos de un pedido")
    public OrderResponse update(@PathVariable String id, @Valid @RequestBody UpdateOrderRequest request) {
        return orders.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambia el estatus y lo agrega al historial")
    public OrderResponse changeStatus(@PathVariable String id, @Valid @RequestBody ChangeStatusRequest request) {
        return orders.changeStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un pedido")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        orders.delete(id);
        return ResponseEntity.noContent().build();
    }
}
