package mx.com.liverpool.pedidos.servicio.impl;

import mx.com.liverpool.pedidos.comun.Identificadores;
import mx.com.liverpool.pedidos.comun.Importes;
import mx.com.liverpool.pedidos.comun.Textos;
import mx.com.liverpool.pedidos.dominio.DatosCliente;
import mx.com.liverpool.pedidos.dominio.EstatusPedido;
import mx.com.liverpool.pedidos.dominio.Pedido;
import mx.com.liverpool.pedidos.dominio.Usuario;
import mx.com.liverpool.pedidos.repositorio.PedidoBusqueda;
import mx.com.liverpool.pedidos.repositorio.PedidoRepository;
import mx.com.liverpool.pedidos.repositorio.ResultadoBusqueda;
import mx.com.liverpool.pedidos.repositorio.UsuarioRepository;
import mx.com.liverpool.pedidos.servicio.ArmadorSugerencias;
import mx.com.liverpool.pedidos.servicio.FolioService;
import mx.com.liverpool.pedidos.servicio.PedidoService;
import mx.com.liverpool.pedidos.web.dto.ActualizarPedidoSolicitud;
import mx.com.liverpool.pedidos.web.dto.CrearPedidoSolicitud;
import mx.com.liverpool.pedidos.web.dto.PaginaRespuesta;
import mx.com.liverpool.pedidos.web.dto.PedidoRespuesta;
import mx.com.liverpool.pedidos.web.dto.SugerenciaRespuesta;
import mx.com.liverpool.pedidos.web.error.RecursoNoEncontradoException;
import mx.com.liverpool.pedidos.web.error.ReglaNegocioException;
import mx.com.liverpool.pedidos.web.mapeo.PedidoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final PedidoBusqueda busqueda;
    private final FolioService folios;
    private final ArmadorSugerencias sugerencias;
    private final PedidoMapper mapper;

    public PedidoServiceImpl(PedidoRepository pedidos, UsuarioRepository usuarios, PedidoBusqueda busqueda,
                             FolioService folios, ArmadorSugerencias sugerencias, PedidoMapper mapper) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.busqueda = busqueda;
        this.folios = folios;
        this.sugerencias = sugerencias;
        this.mapper = mapper;
    }

    @Override
    public PedidoRespuesta crear(CrearPedidoSolicitud solicitud) {
        Usuario usuario = buscarUsuario(solicitud.getUsuarioId());
        Instant ahora = Instant.now();
        EstatusPedido estatus = solicitud.getEstatus() == null ? EstatusPedido.RECIBIDO : solicitud.getEstatus();

        Pedido pedido = new Pedido();
        pedido.setFolio(folios.siguiente());
        pedido.setUsuarioId(usuario.getId());
        pedido.setCliente(DatosCliente.desde(usuario));
        aplicarDatos(pedido, solicitud.getDireccionEnvio(), solicitud.getCodigoProducto(),
                solicitud.getDescripcionProducto(), solicitud.getCantidad(), solicitud.getPrecio(), solicitud.getCanal());
        pedido.setCreadoEn(ahora);
        pedido.registrarEstatus(estatus, ahora);
        pedido.setTextoBusqueda(Textos.indiceDe(pedido));
        return mapper.aRespuesta(pedidos.save(pedido));
    }

    @Override
    public PaginaRespuesta<PedidoRespuesta> listar(String termino, String estatus, int pagina, int tamano) {
        if (pagina < 0) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La página no puede ser negativa.");
        }
        if (tamano < 1 || tamano > 100) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El tamaño de página debe estar entre 1 y 100.");
        }
        ResultadoBusqueda resultado = busqueda.buscar(termino, parsearEstatus(estatus), pagina, tamano);
        List<PedidoRespuesta> contenido = new ArrayList<PedidoRespuesta>();
        for (Pedido pedido : resultado.getPedidos()) {
            contenido.add(mapper.aRespuesta(pedido));
        }
        return PaginaRespuesta.de(contenido, pagina, tamano, resultado.getTotal());
    }

    @Override
    public PedidoRespuesta obtener(String id) {
        return mapper.aRespuesta(buscar(id));
    }

    @Override
    public PedidoRespuesta actualizar(String id, ActualizarPedidoSolicitud solicitud) {
        Pedido pedido = buscar(id);
        aplicarDatos(pedido, solicitud.getDireccionEnvio(), solicitud.getCodigoProducto(),
                solicitud.getDescripcionProducto(), solicitud.getCantidad(), solicitud.getPrecio(), solicitud.getCanal());
        if (pedido.getEstatus() != solicitud.getEstatus()) {
            pedido.registrarEstatus(solicitud.getEstatus(), Instant.now());
        } else {
            pedido.setActualizadoEn(Instant.now());
        }
        pedido.setTextoBusqueda(Textos.indiceDe(pedido));
        return mapper.aRespuesta(pedidos.save(pedido));
    }

    @Override
    public PedidoRespuesta cambiarEstatus(String id, EstatusPedido estatus) {
        Pedido pedido = buscar(id);
        if (pedido.getEstatus() != estatus) {
            pedido.registrarEstatus(estatus, Instant.now());
            pedido.setTextoBusqueda(Textos.indiceDe(pedido));
            pedido = pedidos.save(pedido);
        }
        return mapper.aRespuesta(pedido);
    }

    @Override
    public void eliminar(String id) {
        Pedido pedido = buscar(id);
        pedidos.deleteById(pedido.getId());
    }

    @Override
    public List<SugerenciaRespuesta> sugerir(String termino) {
        if (Textos.normalizar(termino).length() < 2) {
            return sugerencias.armar(termino, new ArrayList<Pedido>());
        }
        return sugerencias.armar(termino, busqueda.coincidencias(termino, 15));
    }

    private void aplicarDatos(Pedido pedido, String direccion, String codigo, String descripcion,
                              int cantidad, java.math.BigDecimal precio, mx.com.liverpool.pedidos.dominio.CanalPedido canal) {
        pedido.setDireccionEnvio(Textos.limpio(direccion));
        pedido.setCodigoProducto(Textos.limpio(codigo).toUpperCase(Locale.ROOT));
        pedido.setDescripcionProducto(Textos.limpio(descripcion));
        pedido.setCantidad(cantidad);
        pedido.setPrecio(Importes.dinero(precio));
        pedido.setCanal(canal);
    }

    private Pedido buscar(String id) {
        Identificadores.exigir(id, "Pedido no encontrado.");
        return pedidos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado."));
    }

    private Usuario buscarUsuario(String id) {
        Identificadores.exigir(id, "Cliente no encontrado.");
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado."));
    }

    private EstatusPedido parsearEstatus(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return EstatusPedido.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "Estatus no reconocido.");
        }
    }
}
