package mx.com.liverpool.pedidos.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.pedidos.dominio.EstatusPedido;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class EventoEstatusRespuesta {

    private EstatusPedido estatus;
    private Instant ocurridoEn;
}
