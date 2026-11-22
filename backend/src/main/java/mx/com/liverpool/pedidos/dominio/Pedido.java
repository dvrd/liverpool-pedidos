package mx.com.liverpool.pedidos.dominio;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id;

    @Indexed(unique = true)
    private String folio;

    @Indexed
    private String usuarioId;

    private DatosCliente cliente;
    private String direccionEnvio;

    @Indexed
    private String codigoProducto;

    private String descripcionProducto;
    private int cantidad;
    private BigDecimal precio;
    private CanalPedido canal;

    @Indexed
    private EstatusPedido estatus;

    private List<EventoEstatus> historial = new ArrayList<EventoEstatus>();

    /**
     * Copia de búsqueda en minúsculas y sin acentos. Se reconstruye en cada escritura
     * para que el typeahead y el filtro usen el mismo criterio.
     */
    private String textoBusqueda;

    private Instant creadoEn;
    private Instant actualizadoEn;

    public void registrarEstatus(EstatusPedido nuevo, Instant cuando) {
        if (historial == null) {
            historial = new ArrayList<EventoEstatus>();
        }
        historial.add(EventoEstatus.de(nuevo, cuando));
        this.estatus = nuevo;
        this.actualizadoEn = cuando;
    }
}
