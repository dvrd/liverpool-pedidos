package mx.com.liverpool.pedidos.web.mapeo;

import mx.com.liverpool.pedidos.comun.Importes;
import mx.com.liverpool.pedidos.dominio.DatosCliente;
import mx.com.liverpool.pedidos.dominio.EventoEstatus;
import mx.com.liverpool.pedidos.dominio.Pedido;
import mx.com.liverpool.pedidos.web.dto.ClienteRespuesta;
import mx.com.liverpool.pedidos.web.dto.EventoEstatusRespuesta;
import mx.com.liverpool.pedidos.web.dto.PedidoRespuesta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class PedidoMapper {

    public PedidoRespuesta aRespuesta(Pedido pedido) {
        PedidoRespuesta respuesta = new PedidoRespuesta();
        respuesta.setId(pedido.getId());
        respuesta.setFolio(pedido.getFolio());
        respuesta.setCanal(pedido.getCanal());
        respuesta.setUsuarioId(pedido.getUsuarioId());
        respuesta.setCliente(aCliente(pedido.getCliente()));
        respuesta.setDireccionEnvio(pedido.getDireccionEnvio());
        respuesta.setCodigoProducto(pedido.getCodigoProducto());
        respuesta.setDescripcionProducto(pedido.getDescripcionProducto());
        respuesta.setCantidad(pedido.getCantidad());
        respuesta.setPrecio(pedido.getPrecio());
        respuesta.setImporteTotal(Importes.total(pedido.getPrecio(), pedido.getCantidad()));
        respuesta.setEstatus(pedido.getEstatus());
        respuesta.setHistorial(aHistorial(pedido.getHistorial()));
        respuesta.setCreadoEn(pedido.getCreadoEn());
        respuesta.setActualizadoEn(pedido.getActualizadoEn());
        return respuesta;
    }

    private ClienteRespuesta aCliente(DatosCliente cliente) {
        if (cliente == null) {
            return null;
        }
        ClienteRespuesta respuesta = new ClienteRespuesta();
        respuesta.setNombre(cliente.getNombre());
        respuesta.setApellidoPaterno(cliente.getApellidoPaterno());
        respuesta.setApellidoMaterno(cliente.getApellidoMaterno());
        respuesta.setNombreCompleto(cliente.nombreCompleto());
        respuesta.setCorreoElectronico(cliente.getCorreoElectronico());
        return respuesta;
    }

    private List<EventoEstatusRespuesta> aHistorial(List<EventoEstatus> historial) {
        if (historial == null || historial.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventoEstatusRespuesta> respuesta = new ArrayList<EventoEstatusRespuesta>();
        for (EventoEstatus evento : historial) {
            EventoEstatusRespuesta item = new EventoEstatusRespuesta();
            item.setEstatus(evento.getEstatus());
            item.setOcurridoEn(evento.getOcurridoEn());
            respuesta.add(item);
        }
        return respuesta;
    }
}
