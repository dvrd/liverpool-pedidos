package mx.com.liverpool.pedidos.servicio;

import mx.com.liverpool.pedidos.dominio.EstatusPedido;
import mx.com.liverpool.pedidos.web.dto.ActualizarPedidoSolicitud;
import mx.com.liverpool.pedidos.web.dto.CrearPedidoSolicitud;
import mx.com.liverpool.pedidos.web.dto.PaginaRespuesta;
import mx.com.liverpool.pedidos.web.dto.PedidoRespuesta;
import mx.com.liverpool.pedidos.web.dto.SugerenciaRespuesta;

import java.util.List;

public interface PedidoService {

    PedidoRespuesta crear(CrearPedidoSolicitud solicitud);

    PaginaRespuesta<PedidoRespuesta> listar(String termino, String estatus, int pagina, int tamano);

    PedidoRespuesta obtener(String id);

    PedidoRespuesta actualizar(String id, ActualizarPedidoSolicitud solicitud);

    PedidoRespuesta cambiarEstatus(String id, EstatusPedido estatus);

    void eliminar(String id);

    List<SugerenciaRespuesta> sugerir(String termino);
}
