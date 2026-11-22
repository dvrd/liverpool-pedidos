package mx.com.liverpool.pedidos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.pedidos.dominio.EstatusPedido;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class CambiarEstatusSolicitud {

    @NotNull(message = "El estatus es obligatorio")
    @Schema(example = "ENVIADO")
    private EstatusPedido estatus;
}
