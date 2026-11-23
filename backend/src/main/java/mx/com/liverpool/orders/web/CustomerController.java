package mx.com.liverpool.orders.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.com.liverpool.orders.service.CustomerService;
import mx.com.liverpool.orders.web.dto.CreateCustomerRequest;
import mx.com.liverpool.orders.web.dto.CustomerRecord;
import mx.com.liverpool.orders.web.dto.UpdateCustomerRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Clientes", description = "Clientes que realizan pedidos desde la aplicación o el sitio web")
public class CustomerController {

    private final CustomerService customers;

    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @PostMapping
    @Operation(summary = "Crea un cliente")
    public ResponseEntity<CustomerRecord> create(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerRecord created = customers.create(request);
        return ResponseEntity.created(ResourceLocation.forId(created.getId())).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista los clientes")
    public List<CustomerRecord> list() {
        return customers.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un cliente")
    public CustomerRecord get(@PathVariable String id) {
        return customers.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de un cliente")
    public CustomerRecord update(@PathVariable String id, @Valid @RequestBody UpdateCustomerRequest request) {
        return customers.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un cliente sin pedidos")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        customers.delete(id);
        return ResponseEntity.noContent().build();
    }
}
