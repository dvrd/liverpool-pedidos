package mx.com.liverpool.orders.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import mx.com.liverpool.orders.domain.OrderChannel;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
public abstract class OrderFields {

    @NotBlank(message = "La dirección de envío es obligatoria")
    @Size(max = 240, message = "La dirección de envío admite hasta 240 caracteres")
    @Schema(example = "Av. Presidente Masaryk 111, Polanco, Miguel Hidalgo, CDMX, 11560")
    private String shippingAddress;

    @NotBlank(message = "El código del producto es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9-]{4,32}$", message = "El código del producto usa letras, números y guiones")
    @Schema(example = "LP-MOD-10442")
    private String productCode;

    @NotBlank(message = "La descripción del producto es obligatoria")
    @Size(max = 140, message = "La descripción admite hasta 140 caracteres")
    @Schema(example = "Vestido midi de lino")
    private String productDescription;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    @Max(value = 99, message = "La cantidad máxima es 99")
    @Schema(example = "1")
    private Integer quantity;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
    @Schema(example = "1890.00", description = "Precio unitario en MXN")
    private BigDecimal unitPrice;

    @NotNull(message = "El canal es obligatorio")
    @Schema(example = "WEBSITE", allowableValues = {"APP", "WEBSITE"})
    private OrderChannel channel;
}
