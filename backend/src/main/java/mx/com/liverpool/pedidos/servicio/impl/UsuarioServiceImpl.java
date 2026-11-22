package mx.com.liverpool.pedidos.servicio.impl;

import mx.com.liverpool.pedidos.comun.Identificadores;
import mx.com.liverpool.pedidos.comun.Textos;
import mx.com.liverpool.pedidos.dominio.Usuario;
import mx.com.liverpool.pedidos.repositorio.PedidoRepository;
import mx.com.liverpool.pedidos.repositorio.UsuarioRepository;
import mx.com.liverpool.pedidos.servicio.UsuarioService;
import mx.com.liverpool.pedidos.web.dto.ActualizarUsuarioSolicitud;
import mx.com.liverpool.pedidos.web.dto.CrearUsuarioSolicitud;
import mx.com.liverpool.pedidos.web.dto.UsuarioRespuesta;
import mx.com.liverpool.pedidos.web.error.RecursoNoEncontradoException;
import mx.com.liverpool.pedidos.web.error.ReglaNegocioException;
import mx.com.liverpool.pedidos.web.mapeo.UsuarioMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;
    private final UsuarioMapper mapper;

    public UsuarioServiceImpl(UsuarioRepository usuarios, PedidoRepository pedidos, UsuarioMapper mapper) {
        this.usuarios = usuarios;
        this.pedidos = pedidos;
        this.mapper = mapper;
    }

    @Override
    public UsuarioRespuesta crear(CrearUsuarioSolicitud solicitud) {
        String correo = Textos.correo(solicitud.getCorreoElectronico());
        if (usuarios.findByCorreoElectronicoIgnoreCase(correo).isPresent()) {
            throw correoDuplicado();
        }
        Instant ahora = Instant.now();
        Usuario usuario = new Usuario();
        usuario.setNombre(Textos.limpio(solicitud.getNombre()));
        usuario.setApellidoPaterno(Textos.limpio(solicitud.getApellidoPaterno()));
        usuario.setApellidoMaterno(Textos.limpio(solicitud.getApellidoMaterno()));
        usuario.setCorreoElectronico(correo);
        usuario.setCreadoEn(ahora);
        usuario.setActualizadoEn(ahora);
        return mapper.aRespuesta(guardar(usuario));
    }

    @Override
    public List<UsuarioRespuesta> listar() {
        List<UsuarioRespuesta> respuesta = new ArrayList<UsuarioRespuesta>();
        for (Usuario usuario : usuarios.findAllByOrderByNombreAscApellidoPaternoAsc()) {
            respuesta.add(mapper.aRespuesta(usuario));
        }
        return respuesta;
    }

    @Override
    public UsuarioRespuesta obtener(String id) {
        return mapper.aRespuesta(buscar(id));
    }

    @Override
    public UsuarioRespuesta actualizar(String id, ActualizarUsuarioSolicitud solicitud) {
        Usuario usuario = buscar(id);
        String correo = Textos.correo(solicitud.getCorreoElectronico());
        Optional<Usuario> otro = usuarios.findByCorreoElectronicoIgnoreCase(correo);
        if (otro.isPresent() && !otro.get().getId().equals(usuario.getId())) {
            throw correoDuplicado();
        }
        usuario.setNombre(Textos.limpio(solicitud.getNombre()));
        usuario.setApellidoPaterno(Textos.limpio(solicitud.getApellidoPaterno()));
        usuario.setApellidoMaterno(Textos.limpio(solicitud.getApellidoMaterno()));
        usuario.setCorreoElectronico(correo);
        usuario.setActualizadoEn(Instant.now());
        return mapper.aRespuesta(guardar(usuario));
    }

    @Override
    public void eliminar(String id) {
        Usuario usuario = buscar(id);
        if (pedidos.existsByUsuarioId(usuario.getId())) {
            throw new ReglaNegocioException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el cliente porque tiene pedidos asociados.");
        }
        usuarios.deleteById(usuario.getId());
    }

    private Usuario buscar(String id) {
        Identificadores.exigir(id, "Cliente no encontrado.");
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado."));
    }

    private Usuario guardar(Usuario usuario) {
        try {
            return usuarios.save(usuario);
        } catch (DuplicateKeyException ex) {
            throw correoDuplicado();
        }
    }

    private ReglaNegocioException correoDuplicado() {
        return new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe un cliente con ese correo electrónico.");
    }
}
