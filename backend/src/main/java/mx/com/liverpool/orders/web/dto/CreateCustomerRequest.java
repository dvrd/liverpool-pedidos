package mx.com.liverpool.orders.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Alta de un cliente")
public class CreateCustomerRequest extends CustomerFields {
}
