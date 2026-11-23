package mx.com.liverpool.orders.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    private Money() {
    }

    public static BigDecimal amount(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal total(BigDecimal price, int quantity) {
        return amount(price).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
