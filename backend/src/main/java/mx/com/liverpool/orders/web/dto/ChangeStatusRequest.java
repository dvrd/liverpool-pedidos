package mx.com.liverpool.orders.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.orders.domain.OrderStatus;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ChangeStatusRequest {

    @NotNull(message = "El estatus es obligatorio")
    @Schema(example = "SHIPPED")
    private OrderStatus status;
}
