package mx.com.liverpool.pedidos.web.mapeo;

import mx.com.liverpool.pedidos.dominio.Usuario;
import mx.com.liverpool.pedidos.web.dto.UsuarioRespuesta;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioRespuesta aRespuesta(Usuario usuario) {
        UsuarioRespuesta respuesta = new UsuarioRespuesta();
        respuesta.setId(usuario.getId());
        respuesta.setNombre(usuario.getNombre());
        respuesta.setApellidoPaterno(usuario.getApellidoPaterno());
        respuesta.setApellidoMaterno(usuario.getApellidoMaterno());
        respuesta.setNombreCompleto(usuario.getNombre() + " " + usuario.getApellidoPaterno() + " " + usuario.getApellidoMaterno());
        respuesta.setCorreoElectronico(usuario.getCorreoElectronico());
        respuesta.setCreadoEn(usuario.getCreadoEn());
        respuesta.setActualizadoEn(usuario.getActualizadoEn());
        return respuesta;
    }
}
