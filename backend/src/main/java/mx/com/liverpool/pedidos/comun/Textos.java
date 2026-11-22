package mx.com.liverpool.pedidos.comun;

import mx.com.liverpool.pedidos.dominio.CanalPedido;
import mx.com.liverpool.pedidos.dominio.DatosCliente;
import mx.com.liverpool.pedidos.dominio.Pedido;

import java.text.Normalizer;
import java.util.Locale;

public final class Textos {

    private Textos() {
    }

    public static String limpio(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().replaceAll("\\s+", " ");
    }

    public static String correo(String valor) {
        return limpio(valor).toLowerCase(Locale.ROOT);
    }

    public static String normalizar(String valor) {
        String base = limpio(valor);
        if (base.isEmpty()) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }

    /**
     * MongoDB evalúa el patrón con PCRE. {@code Pattern.quote} usa \\Q y \\E, que PCRE no trata
     * como cita literal, así que los metacaracteres se escapan uno a uno.
     */
    public static String regexLiteral(String valor) {
        return normalizar(valor).replaceAll("([\\\\.^$|?*+()\\[\\]{}])", "\\\\$1");
    }

    public static String indiceDe(Pedido pedido) {
        String canal = pedido.getCanal() == CanalPedido.APLICACION ? "aplicacion app" : "sitio web";
        DatosCliente cliente = pedido.getCliente();
        String datosCliente = cliente == null ? "" : String.join(" ",
                nulo(cliente.getNombre()),
                nulo(cliente.getApellidoPaterno()),
                nulo(cliente.getApellidoMaterno()),
                nulo(cliente.getCorreoElectronico()));
        String estatus = pedido.getEstatus() == null ? "" : pedido.getEstatus().name();
        return normalizar(String.join(" ",
                nulo(pedido.getFolio()),
                datosCliente,
                nulo(pedido.getDireccionEnvio()),
                nulo(pedido.getCodigoProducto()),
                nulo(pedido.getDescripcionProducto()),
                canal,
                estatus));
    }

    private static String nulo(String valor) {
        return valor == null ? "" : valor;
    }
}
