package mx.com.liverpool.orders.common;

import mx.com.liverpool.orders.web.error.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentifiersTest {

    @Test
    void acceptsAMongoObjectId() {
        assertThatCode(() -> Identifiers.require("507f1f77bcf86cd799439011", "Pedido no encontrado."))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAnythingThatIsNotAnObjectId() {
        assertThatThrownBy(() -> Identifiers.require(null, "Pedido no encontrado."))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Pedido no encontrado.");
        assertThatThrownBy(() -> Identifiers.require("507f1f77bcf86cd79943901", "Cliente no encontrado."))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Cliente no encontrado.");
    }
}
