package mx.com.liverpool.pedidos.comun;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ImportesTest {

    @Test
    void multiplicaPrecioPorCantidad() {
        assertThat(Importes.total(new BigDecimal("1650.00"), 2)).isEqualByComparingTo("3300.00");
        assertThat(Importes.total(new BigDecimal("420"), 3)).isEqualByComparingTo("1260.00");
    }
}
