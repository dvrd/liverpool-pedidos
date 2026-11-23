package mx.com.liverpool.orders.common;

import mx.com.liverpool.orders.domain.Customer;
import mx.com.liverpool.orders.domain.CustomerSnapshot;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.OrderChannel;
import mx.com.liverpool.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextTest {

    @Test
    void stripsAccentsAndLowercases() {
        assertThat(Text.normalize("José Núñez")).isEqualTo("jose nunez");
        assertThat(Text.normalize("  Camila   Sofía ")).isEqualTo("camila sofia");
    }

    @Test
    void escapesMetacharactersForMongo() {
        assertThat(Text.literalRegex("LP-ELC")).isEqualTo("lp-elc");
        assertThat(Text.literalRegex("a.b")).isEqualTo("a\\.b");
        assertThat(Text.literalRegex("10+2")).isEqualTo("10\\+2");
    }

    @Test
    void normalizesEmail() {
        assertThat(Text.email("  Ana@Correo.COM ")).isEqualTo("ana@correo.com");
    }

    @Test
    void joinsAPersonNameAndDropsEmptyParts() {
        assertThat(Text.personName("Ana", "López", null)).isEqualTo("Ana López");
        assertThat(Text.personName("  Ana ", "López", " Díaz ")).isEqualTo("Ana López Díaz");
    }

    @Test
    void buildsTheSearchIndexFromTheWordsASellerTypes() {
        Customer customer = new Customer();
        customer.setFirstName("José");
        customer.setPaternalLastName("Núñez");
        customer.setMaternalLastName("Solís");
        customer.setEmail("Jose.Nunez@correo.com");
        Order order = new Order();
        order.setOrderNumber("LIV-000002");
        order.setCustomer(CustomerSnapshot.from(customer));
        order.setShippingAddress("Álvaro Obregón 45");
        order.setProductCode("LP-PER-22018");
        order.setProductDescription("Eau de parfum");
        order.setChannel(OrderChannel.WEBSITE);
        order.setStatus(OrderStatus.SHIPPED);

        assertThat(Text.searchIndex(order)).isEqualTo(
                "liv-000002 jose nunez solis jose.nunez@correo.com alvaro obregon 45 lp-per-22018 eau de parfum sitio web enviado");
    }

    @Test
    void aMissingChannelIsNotIndexedAsTheWebsite() {
        Order order = new Order();
        order.setOrderNumber("LIV-000001");
        order.setStatus(OrderStatus.RECEIVED);

        assertThat(Text.searchIndex(order)).doesNotContain("sitio").doesNotContain("web");
        assertThat(Text.searchIndex(order)).contains("recibido");
    }
}
