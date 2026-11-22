package mx.com.liverpool.pedidos.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.com.liverpool.pedidos.servicio.UsuarioService;
import mx.com.liverpool.pedidos.web.dto.ActualizarUsuarioSolicitud;
import mx.com.liverpool.pedidos.web.dto.CrearUsuarioSolicitud;
import mx.com.liverpool.pedidos.web.dto.UsuarioRespuesta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Clientes que realizan pedidos desde la aplicación o el sitio web")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @PostMapping
    @Operation(summary = "Crea un cliente")
    public ResponseEntity<UsuarioRespuesta> crear(@Valid @RequestBody CrearUsuarioSolicitud solicitud) {
        UsuarioRespuesta creado = usuarios.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping
    @Operation(summary = "Lista los clientes")
    public List<UsuarioRespuesta> listar() {
        return usuarios.listar();
    }

    @GetMapping("/{id:[a-fA-F0-9]+}")
    @Operation(summary = "Consulta un cliente")
    public UsuarioRespuesta obtener(@PathVariable String id) {
        return usuarios.obtener(id);
    }

    @PutMapping("/{id:[a-fA-F0-9]+}")
    @Operation(summary = "Actualiza los datos de un cliente")
    public UsuarioRespuesta actualizar(@PathVariable String id, @Valid @RequestBody ActualizarUsuarioSolicitud solicitud) {
        return usuarios.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id:[a-fA-F0-9]+}")
    @Operation(summary = "Elimina un cliente sin pedidos")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        usuarios.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
