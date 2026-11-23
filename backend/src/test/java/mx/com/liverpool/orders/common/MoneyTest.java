package mx.com.liverpool.orders.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void multipliesPriceByQuantity() {
        assertThat(Money.total(new BigDecimal("1650.00"), 2)).isEqualByComparingTo("3300.00");
        assertThat(Money.total(new BigDecimal("420"), 3)).isEqualByComparingTo("1260.00");
    }

    @Test
    void roundsHalfUpToCents() {
        assertThat(Money.amount(new BigDecimal("10.005"))).isEqualByComparingTo("10.01");
        assertThat(Money.amount(new BigDecimal("10.004"))).isEqualByComparingTo("10.00");
    }
}
