package mx.com.liverpool.pedidos.servicio;

import mx.com.liverpool.pedidos.dominio.DatosCliente;
import mx.com.liverpool.pedidos.dominio.Pedido;
import mx.com.liverpool.pedidos.web.dto.SugerenciaRespuesta;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArmadorSugerenciasTest {

    private final ArmadorSugerencias armador = new ArmadorSugerencias();

    @Test
    void encuentraPorNombreSinAcentosYNoRepiteAlCliente() {
        Pedido primero = pedido("LIV-000001", "José", "Herrera", "Díaz", "jose.herrera@correo.com", "LP-ELC-77810", "Audífonos");
        Pedido segundo = pedido("LIV-000013", "José", "Herrera", "Díaz", "jose.herrera@correo.com", "LP-HOG-35220", "Cafetera");

        List<SugerenciaRespuesta> sugerencias = armador.armar("jose", Arrays.asList(primero, segundo));

        assertThat(sugerencias).extracting(SugerenciaRespuesta::getTipo).contains("CLIENTE", "CORREO");
        assertThat(sugerencias).filteredOn(s -> "CLIENTE".equals(s.getTipo())).hasSize(1);
        assertThat(sugerencias).filteredOn(s -> "CLIENTE".equals(s.getTipo()))
                .extracting(SugerenciaRespuesta::getValor)
                .containsExactly("José Herrera Díaz");
    }

    @Test
    void terminoCortoNoSugiere() {
        Pedido pedido = pedido("LIV-000001", "Ana", "Ruiz", "Solis", "ana@correo.com", "LP-MOD-1", "Vestido");
        assertThat(armador.armar("a", Arrays.asList(pedido))).isEmpty();
    }

    @Test
    void sugiereElCodigoCuandoElTerminoVaEnElProducto() {
        Pedido pedido = pedido("LIV-000002", "Diego", "Paredes", "Cruz", "diego@correo.com", "LP-ELC-79015", "Barra de sonido");

        List<SugerenciaRespuesta> sugerencias = armador.armar("LP-ELC", Arrays.asList(pedido));

        assertThat(sugerencias).extracting(SugerenciaRespuesta::getValor).contains("LP-ELC-79015");
    }

    private Pedido pedido(String folio, String nombre, String paterno, String materno, String correo,
                          String codigo, String descripcion) {
        DatosCliente cliente = new DatosCliente();
        cliente.setNombre(nombre);
        cliente.setApellidoPaterno(paterno);
        cliente.setApellidoMaterno(materno);
        cliente.setCorreoElectronico(correo);
        Pedido pedido = new Pedido();
        pedido.setFolio(folio);
        pedido.setCliente(cliente);
        pedido.setCodigoProducto(codigo);
        pedido.setDescripcionProducto(descripcion);
        return pedido;
    }
}
