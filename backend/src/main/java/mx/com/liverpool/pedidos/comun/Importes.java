package mx.com.liverpool.pedidos.comun;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Importes {

    private Importes() {
    }

    public static BigDecimal dinero(BigDecimal precio) {
        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal total(BigDecimal precio, int cantidad) {
        return dinero(precio).multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }
}
