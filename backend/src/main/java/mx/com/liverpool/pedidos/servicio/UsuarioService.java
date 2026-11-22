package mx.com.liverpool.pedidos.servicio;

import mx.com.liverpool.pedidos.web.dto.ActualizarUsuarioSolicitud;
import mx.com.liverpool.pedidos.web.dto.CrearUsuarioSolicitud;
import mx.com.liverpool.pedidos.web.dto.UsuarioRespuesta;

import java.util.List;

public interface UsuarioService {

    UsuarioRespuesta crear(CrearUsuarioSolicitud solicitud);

    List<UsuarioRespuesta> listar();

    UsuarioRespuesta obtener(String id);

    UsuarioRespuesta actualizar(String id, ActualizarUsuarioSolicitud solicitud);

    void eliminar(String id);
}
