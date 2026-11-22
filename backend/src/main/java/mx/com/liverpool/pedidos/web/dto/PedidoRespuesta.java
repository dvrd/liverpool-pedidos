package mx.com.liverpool.pedidos.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.pedidos.dominio.CanalPedido;
import mx.com.liverpool.pedidos.dominio.EstatusPedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PedidoRespuesta {

    private String id;
    private String folio;
    private CanalPedido canal;
    private String usuarioId;
    private ClienteRespuesta cliente;
    private String direccionEnvio;
    private String codigoProducto;
    private String descripcionProducto;
    private int cantidad;
    private BigDecimal precio;
    private BigDecimal importeTotal;
    private EstatusPedido estatus;
    private List<EventoEstatusRespuesta> historial = new ArrayList<EventoEstatusRespuesta>();
    private Instant creadoEn;
    private Instant actualizadoEn;
}
