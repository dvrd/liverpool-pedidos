package mx.com.liverpool.pedidos.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.com.liverpool.pedidos.servicio.PedidoService;
import mx.com.liverpool.pedidos.web.dto.ActualizarPedidoSolicitud;
import mx.com.liverpool.pedidos.web.dto.CambiarEstatusSolicitud;
import mx.com.liverpool.pedidos.web.dto.CrearPedidoSolicitud;
import mx.com.liverpool.pedidos.web.dto.PaginaRespuesta;
import mx.com.liverpool.pedidos.web.dto.PedidoRespuesta;
import mx.com.liverpool.pedidos.web.dto.SugerenciaRespuesta;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Pedidos originados en la aplicación y en el sitio web")
public class PedidoController {

    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @PostMapping
    @Operation(summary = "Crea un pedido")
    public ResponseEntity<PedidoRespuesta> crear(@Valid @RequestBody CrearPedidoSolicitud solicitud) {
        PedidoRespuesta creado = pedidos.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping
    @Operation(summary = "Lista pedidos, con filtro por término y estatus")
    public PaginaRespuesta<PedidoRespuesta> listar(
            @RequestParam(value = "termino", required = false) String termino,
            @RequestParam(value = "estatus", required = false) String estatus,
            @RequestParam(value = "pagina", defaultValue = "0") int pagina,
            @RequestParam(value = "tamano", defaultValue = "20") int tamano) {
        return pedidos.listar(termino, estatus, pagina, tamano);
    }

    @GetMapping("/sugerencias")
    @Operation(summary = "Opciones del typeahead a partir de dos caracteres")
    public List<SugerenciaRespuesta> sugerencias(@RequestParam(value = "termino", required = false) String termino) {
        return pedidos.sugerir(termino);
    }

    @GetMapping("/{id:[a-fA-F0-9]+}")
    @Operation(summary = "Consulta el detalle de un pedido")
    public PedidoRespuesta obtener(@PathVariable String id) {
        return pedidos.obtener(id);
    }

    @PutMapping("/{id:[a-fA-F0-9]+}")
    @Operation(summary = "Reemplaza los datos operativos de un pedido")
    public PedidoRespuesta actualizar(@PathVariable String id, @Valid @RequestBody ActualizarPedidoSolicitud solicitud) {
        return pedidos.actualizar(id, solicitud);
    }

    @PatchMapping("/{id:[a-fA-F0-9]+}/estatus")
    @Operation(summary = "Cambia el estatus y lo agrega al historial")
    public PedidoRespuesta cambiarEstatus(@PathVariable String id, @Valid @RequestBody CambiarEstatusSolicitud solicitud) {
        return pedidos.cambiarEstatus(id, solicitud.getEstatus());
    }

    @DeleteMapping("/{id:[a-fA-F0-9]+}")
    @Operation(summary = "Elimina un pedido")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        pedidos.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
