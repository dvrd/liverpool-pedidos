package mx.com.liverpool.orders.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.orders.domain.OrderStatus;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Alta de un pedido asociado a un cliente existente")
public class CreateOrderRequest extends OrderFields {

    @NotBlank(message = "El cliente es obligatorio")
    @Schema(description = "Identificador del cliente", example = "665f1c2b8a0d1e2f3a4b5c6d")
    private String customerId;

    @Schema(description = "Si se omite, el pedido queda recibido.", example = "RECEIVED")
    private OrderStatus status;
}
