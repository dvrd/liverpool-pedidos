package mx.com.liverpool.pedidos.servicio;

import mx.com.liverpool.pedidos.comun.Textos;
import mx.com.liverpool.pedidos.dominio.DatosCliente;
import mx.com.liverpool.pedidos.dominio.Pedido;
import mx.com.liverpool.pedidos.web.dto.SugerenciaRespuesta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class ArmadorSugerencias {

    static final int MAXIMO = 8;

    public List<SugerenciaRespuesta> armar(String termino, List<Pedido> pedidos) {
        String aguja = Textos.normalizar(termino);
        if (aguja.length() < 2 || pedidos == null || pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        List<SugerenciaRespuesta> salida = new ArrayList<SugerenciaRespuesta>();
        Set<String> vistos = new LinkedHashSet<String>();
        for (Pedido pedido : pedidos) {
            DatosCliente cliente = pedido.getCliente();
            String nombre = cliente == null ? "" : cliente.nombreCompleto();
            String correo = cliente == null ? "" : cliente.getCorreoElectronico();
            considerar(salida, vistos, "FOLIO", pedido.getFolio(), nombre, pedido.getFolio(), aguja);
            considerar(salida, vistos, "CLIENTE", nombre, unir(pedido.getFolio(), correo), nombre, aguja);
            considerar(salida, vistos, "CORREO", correo, unir(nombre, pedido.getFolio()), correo, aguja);
            considerar(salida, vistos, "PRODUCTO", pedido.getCodigoProducto(),
                    unir(pedido.getDescripcionProducto(), pedido.getFolio()), pedido.getCodigoProducto(), aguja);
            if (!contiene(pedido.getCodigoProducto(), aguja) && contiene(pedido.getDescripcionProducto(), aguja)) {
                considerar(salida, vistos, "PRODUCTO", pedido.getDescripcionProducto(),
                        unir(pedido.getCodigoProducto(), pedido.getFolio()), pedido.getDescripcionProducto(), aguja);
            }
            if (salida.size() >= MAXIMO) {
                break;
            }
        }
        if (salida.size() > MAXIMO) {
            return new ArrayList<SugerenciaRespuesta>(salida.subList(0, MAXIMO));
        }
        return salida;
    }

    private void considerar(List<SugerenciaRespuesta> salida, Set<String> vistos, String tipo,
                            String etiqueta, String detalle, String valor, String aguja) {
        if (salida.size() >= MAXIMO || etiqueta == null || etiqueta.isEmpty() || valor == null) {
            return;
        }
        if (!contiene(etiqueta, aguja) && !contiene(valor, aguja)) {
            return;
        }
        if (!vistos.add(tipo + "|" + Textos.normalizar(valor))) {
            return;
        }
        salida.add(new SugerenciaRespuesta(tipo, etiqueta, detalle, valor));
    }

    private boolean contiene(String texto, String aguja) {
        return Textos.normalizar(texto).contains(aguja);
    }

    private String unir(String izquierda, String derecha) {
        if (izquierda == null || izquierda.isEmpty()) {
            return derecha == null ? "" : derecha;
        }
        if (derecha == null || derecha.isEmpty()) {
            return izquierda;
        }
        return izquierda + " · " + derecha;
    }
}
