package mx.com.liverpool.pedidos.config;

import lombok.extern.slf4j.Slf4j;
import mx.com.liverpool.pedidos.comun.Textos;
import mx.com.liverpool.pedidos.dominio.CanalPedido;
import mx.com.liverpool.pedidos.dominio.DatosCliente;
import mx.com.liverpool.pedidos.dominio.EstatusPedido;
import mx.com.liverpool.pedidos.dominio.Pedido;
import mx.com.liverpool.pedidos.dominio.Usuario;
import mx.com.liverpool.pedidos.repositorio.PedidoRepository;
import mx.com.liverpool.pedidos.repositorio.UsuarioRepository;
import mx.com.liverpool.pedidos.servicio.FolioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "liverpool.datos-prueba", havingValue = "true", matchIfMissing = true)
public class DatosPruebaRunner implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;
    private final FolioService folios;

    public DatosPruebaRunner(UsuarioRepository usuarios, PedidoRepository pedidos, FolioService folios) {
        this.usuarios = usuarios;
        this.pedidos = pedidos;
        this.folios = folios;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) {
            log.info("La base ya tiene clientes. No se cargan datos de prueba.");
            return;
        }

        Map<String, Usuario> porCorreo = new HashMap<String, Usuario>();
        for (String[] fila : CLIENTES) {
            Usuario usuario = new Usuario();
            usuario.setNombre(fila[0]);
            usuario.setApellidoPaterno(fila[1]);
            usuario.setApellidoMaterno(fila[2]);
            usuario.setCorreoElectronico(fila[3]);
            Instant ahora = Instant.now();
            usuario.setCreadoEn(ahora);
            usuario.setActualizadoEn(ahora);
            porCorreo.put(fila[3], usuarios.save(usuario));
        }

        int indice = 0;
        for (Semilla semilla : PEDIDOS) {
            Usuario usuario = porCorreo.get(semilla.correo);
            Instant creado = Instant.now()
                    .minus(semilla.diasAtras, ChronoUnit.DAYS)
                    .minus(indice, ChronoUnit.HOURS);
            Pedido pedido = new Pedido();
            pedido.setFolio(folios.siguiente());
            pedido.setUsuarioId(usuario.getId());
            pedido.setCliente(DatosCliente.desde(usuario));
            pedido.setDireccionEnvio(semilla.direccion);
            pedido.setCodigoProducto(semilla.codigo);
            pedido.setDescripcionProducto(semilla.descripcion);
            pedido.setCantidad(semilla.cantidad);
            pedido.setPrecio(new BigDecimal(semilla.precio));
            pedido.setCanal(semilla.canal);
            pedido.setCreadoEn(creado);
            for (int paso = 0; paso < semilla.recorrido().size(); paso++) {
                pedido.registrarEstatus(semilla.recorrido().get(paso), creado.plus(paso * 6L, ChronoUnit.HOURS));
            }
            pedido.setTextoBusqueda(Textos.indiceDe(pedido));
            pedidos.save(pedido);
            indice++;
        }

        log.info("Datos de prueba listos: {} clientes y {} pedidos.", porCorreo.size(), indice);
    }

    private static final String[][] CLIENTES = {
            {"Mariana", "Castillo", "Reyes", "mariana.castillo@correo.com"},
            {"José Luis", "Herrera", "Díaz", "jose.herrera@correo.com"},
            {"Andrea Paola", "Núñez", "Solís", "andrea.nunez@correo.com"},
            {"Diego Armando", "Paredes", "Cruz", "diego.paredes@correo.com"},
            {"Fernanda Itzel", "Morales", "Vega", "fernanda.morales@correo.com"},
            {"Ricardo Alonso", "Peña", "Ruiz", "ricardo.pena@correo.com"},
            {"Camila Sofía", "Delgado", "Ortiz", "camila.delgado@correo.com"},
            {"Héctor Iván", "Ramos", "Gil", "hector.ramos@correo.com"}
    };

    private static final List<Semilla> PEDIDOS = Arrays.asList(
            new Semilla("mariana.castillo@correo.com",
                    "Av. Presidente Masaryk 111, Polanco, Miguel Hidalgo, CDMX, 11560",
                    "LP-MOD-10442", "Vestido midi de lino", 1, "1890.00",
                    CanalPedido.SITIO_WEB, EstatusPedido.CONFIRMADO, 6),
            new Semilla("jose.herrera@correo.com",
                    "Calle Orizaba 101, Roma Norte, Cuauhtémoc, CDMX, 06700",
                    "LP-ELC-77810", "Audífonos inalámbricos", 1, "2499.00",
                    CanalPedido.APLICACION, EstatusPedido.ENVIADO, 5),
            new Semilla("andrea.nunez@correo.com",
                    "Francisco Sosa 120, Santa Catarina, Coyoacán, CDMX, 04010",
                    "LP-PER-22018", "Eau de parfum 50 ml", 2, "1650.00",
                    CanalPedido.SITIO_WEB, EstatusPedido.ENTREGADO, 12),
            new Semilla("diego.paredes@correo.com",
                    "Av. Vasco de Quiroga 3800, Santa Fe, Cuajimalpa, CDMX, 05348",
                    "LP-HOG-33007", "Juego de sábanas queen", 1, "1299.00",
                    CanalPedido.APLICACION, EstatusPedido.EN_PREPARACION, 3),
            new Semilla("fernanda.morales@correo.com",
                    "Av. Patria 1891, Jardines Universidad, Zapopan, Jalisco, 45110",
                    "LP-MOD-11890", "Bolso estructurado", 1, "3290.00",
                    CanalPedido.SITIO_WEB, EstatusPedido.RECIBIDO, 1),
            new Semilla("ricardo.pena@correo.com",
                    "Calzada del Valle 400, Del Valle, San Pedro Garza García, N.L., 66220",
                    "LP-ELC-78102", "Pantalla 55 pulgadas", 1, "14999.00",
                    CanalPedido.APLICACION, EstatusPedido.CONFIRMADO, 4),
            new Semilla("camila.delgado@correo.com",
                    "Blvd. Atlixco 1401, La Paz, Puebla, Puebla, 72160",
                    "LP-BEB-45021", "Carriola compacta", 1, "6790.00",
                    CanalPedido.SITIO_WEB, EstatusPedido.ENVIADO, 7),
            new Semilla("hector.ramos@correo.com",
                    "Av. Armando Birlain Shaffler 2001, Centro Sur, Querétaro, Qro., 76090",
                    "LP-DEP-61044", "Tenis para correr", 2, "2190.00",
                    CanalPedido.APLICACION, EstatusPedido.ENTREGADO, 15),
            new Semilla("mariana.castillo@correo.com",
                    "Av. Presidente Masaryk 111, Polanco, Miguel Hidalgo, CDMX, 11560",
                    "LP-HOG-34110", "Licuadora de vaso", 1, "1899.00",
                    CanalPedido.SITIO_WEB, EstatusPedido.CANCELADO, 9),
            new Semilla("andrea.nunez@correo.com",
                    "Francisco Sosa 120, Santa Catarina, Coyoacán, CDMX, 04010",
                    "LP-PER-22190", "Set de crema corporal", 3, "420.00",
                    CanalPedido.APLICACION, EstatusPedido.EN_PREPARACION, 2),
            new Semilla("diego.paredes@correo.com",
                    "Av. Vasco de Quiroga 3800, Santa Fe, Cuajimalpa, CDMX, 05348",
                    "LP-ELC-79015", "Barra de sonido", 1, "3990.00",
                    CanalPedido.SITIO_WEB, EstatusPedido.RECIBIDO, 1),
            new Semilla("fernanda.morales@correo.com",
                    "Av. Patria 1891, Jardines Universidad, Zapopan, Jalisco, 45110",
                    "LP-MOD-12110", "Zapatillas de piel", 1, "2590.00",
                    CanalPedido.APLICACION, EstatusPedido.CONFIRMADO, 8),
            new Semilla("jose.herrera@correo.com",
                    "Calle Orizaba 101, Roma Norte, Cuauhtémoc, CDMX, 06700",
                    "LP-HOG-35220", "Cafetera espresso", 1, "5490.00",
                    CanalPedido.SITIO_WEB, EstatusPedido.ENVIADO, 6),
            new Semilla("camila.delgado@correo.com",
                    "Blvd. Atlixco 1401, La Paz, Puebla, Puebla, 72160",
                    "LP-BEB-45088", "Monitor para bebé", 1, "2890.00",
                    CanalPedido.APLICACION, EstatusPedido.RECIBIDO, 0)
    );

    private static final class Semilla {
        private final String correo;
        private final String direccion;
        private final String codigo;
        private final String descripcion;
        private final int cantidad;
        private final String precio;
        private final CanalPedido canal;
        private final EstatusPedido estatus;
        private final int diasAtras;

        private Semilla(String correo, String direccion, String codigo, String descripcion, int cantidad,
                        String precio, CanalPedido canal, EstatusPedido estatus, int diasAtras) {
            this.correo = correo;
            this.direccion = direccion;
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precio = precio;
            this.canal = canal;
            this.estatus = estatus;
            this.diasAtras = diasAtras;
        }

        private List<EstatusPedido> recorrido() {
            if (estatus == EstatusPedido.CANCELADO) {
                return Arrays.asList(EstatusPedido.RECIBIDO, EstatusPedido.CANCELADO);
            }
            List<EstatusPedido> flujo = Arrays.asList(
                    EstatusPedido.RECIBIDO,
                    EstatusPedido.CONFIRMADO,
                    EstatusPedido.EN_PREPARACION,
                    EstatusPedido.ENVIADO,
                    EstatusPedido.ENTREGADO);
            return flujo.subList(0, flujo.indexOf(estatus) + 1);
        }
    }
}
