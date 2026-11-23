package mx.com.liverpool.orders.config;

import lombok.extern.slf4j.Slf4j;
import mx.com.liverpool.orders.common.Text;
import mx.com.liverpool.orders.domain.Customer;
import mx.com.liverpool.orders.domain.CustomerSnapshot;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.OrderChannel;
import mx.com.liverpool.orders.domain.OrderStatus;
import mx.com.liverpool.orders.repository.CustomerRepository;
import mx.com.liverpool.orders.repository.OrderRepository;
import mx.com.liverpool.orders.service.OrderNumberService;
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
@ConditionalOnProperty(name = "liverpool.sample-data", havingValue = "true", matchIfMissing = true)
public class SampleDataRunner implements CommandLineRunner {

    private final CustomerRepository customers;
    private final OrderRepository orders;
    private final OrderNumberService orderNumbers;

    public SampleDataRunner(CustomerRepository customers, OrderRepository orders, OrderNumberService orderNumbers) {
        this.customers = customers;
        this.orders = orders;
        this.orderNumbers = orderNumbers;
    }

    @Override
    public void run(String... args) {
        if (customers.count() > 0) {
            log.info("Customers already exist. Sample data was not loaded.");
            return;
        }

        Map<String, Customer> byEmail = new HashMap<String, Customer>();
        for (String[] row : CUSTOMERS) {
            Customer customer = new Customer();
            customer.setFirstName(row[0]);
            customer.setPaternalLastName(row[1]);
            customer.setMaternalLastName(row[2]);
            customer.setEmail(row[3]);
            Instant now = Instant.now();
            customer.setCreatedAt(now);
            customer.setUpdatedAt(now);
            byEmail.put(row[3], customers.save(customer));
        }

        int index = 0;
        for (Seed seed : ORDERS) {
            Customer customer = byEmail.get(seed.email);
            Instant created = Instant.now()
                    .minus(seed.daysAgo, ChronoUnit.DAYS)
                    .minus(index, ChronoUnit.HOURS);
            Order order = new Order();
            order.setOrderNumber(orderNumbers.next());
            order.setCustomerId(customer.getId());
            order.setCustomer(CustomerSnapshot.from(customer));
            order.setShippingAddress(seed.address);
            order.setProductCode(seed.code);
            order.setProductDescription(seed.description);
            order.setQuantity(seed.quantity);
            order.setUnitPrice(new BigDecimal(seed.price));
            order.setChannel(seed.channel);
            order.setCreatedAt(created);
            for (int step = 0; step < seed.trail().size(); step++) {
                order.recordStatus(seed.trail().get(step), created.plus(step * 6L, ChronoUnit.HOURS));
            }
            order.setSearchText(Text.searchIndex(order));
            orders.save(order);
            index++;
        }

        log.info("Sample data ready: {} customers and {} orders.", byEmail.size(), index);
    }

    private static final String[][] CUSTOMERS = {
            {"Mariana", "Castillo", "Reyes", "mariana.castillo@correo.com"},
            {"José Luis", "Herrera", "Díaz", "jose.herrera@correo.com"},
            {"Andrea Paola", "Núñez", "Solís", "andrea.nunez@correo.com"},
            {"Diego Armando", "Paredes", "Cruz", "diego.paredes@correo.com"},
            {"Fernanda Itzel", "Morales", "Vega", "fernanda.morales@correo.com"},
            {"Ricardo Alonso", "Peña", "Ruiz", "ricardo.pena@correo.com"},
            {"Camila Sofía", "Delgado", "Ortiz", "camila.delgado@correo.com"},
            {"Héctor Iván", "Ramos", "Gil", "hector.ramos@correo.com"}
    };

    private static final List<Seed> ORDERS = Arrays.asList(
            new Seed("mariana.castillo@correo.com",
                    "Av. Presidente Masaryk 111, Polanco, Miguel Hidalgo, CDMX, 11560",
                    "LP-MOD-10442", "Vestido midi de lino", 1, "1890.00",
                    OrderChannel.WEBSITE, OrderStatus.CONFIRMED, 6),
            new Seed("jose.herrera@correo.com",
                    "Calle Orizaba 101, Roma Norte, Cuauhtémoc, CDMX, 06700",
                    "LP-ELC-77810", "Audífonos inalámbricos", 1, "2499.00",
                    OrderChannel.APP, OrderStatus.SHIPPED, 5),
            new Seed("andrea.nunez@correo.com",
                    "Francisco Sosa 120, Santa Catarina, Coyoacán, CDMX, 04010",
                    "LP-PER-22018", "Eau de parfum 50 ml", 2, "1650.00",
                    OrderChannel.WEBSITE, OrderStatus.DELIVERED, 12),
            new Seed("diego.paredes@correo.com",
                    "Av. Vasco de Quiroga 3800, Santa Fe, Cuajimalpa, CDMX, 05348",
                    "LP-HOG-33007", "Juego de sábanas queen", 1, "1299.00",
                    OrderChannel.APP, OrderStatus.PREPARING, 3),
            new Seed("fernanda.morales@correo.com",
                    "Av. Patria 1891, Jardines Universidad, Zapopan, Jalisco, 45110",
                    "LP-MOD-11890", "Bolso estructurado", 1, "3290.00",
                    OrderChannel.WEBSITE, OrderStatus.RECEIVED, 1),
            new Seed("ricardo.pena@correo.com",
                    "Calzada del Valle 400, Del Valle, San Pedro Garza García, N.L., 66220",
                    "LP-ELC-78102", "Pantalla 55 pulgadas", 1, "14999.00",
                    OrderChannel.APP, OrderStatus.CONFIRMED, 4),
            new Seed("camila.delgado@correo.com",
                    "Blvd. Atlixco 1401, La Paz, Puebla, Puebla, 72160",
                    "LP-BEB-45021", "Carriola compacta", 1, "6790.00",
                    OrderChannel.WEBSITE, OrderStatus.SHIPPED, 7),
            new Seed("hector.ramos@correo.com",
                    "Av. Armando Birlain Shaffler 2001, Centro Sur, Querétaro, Qro., 76090",
                    "LP-DEP-61044", "Tenis para correr", 2, "2190.00",
                    OrderChannel.APP, OrderStatus.DELIVERED, 15),
            new Seed("mariana.castillo@correo.com",
                    "Av. Presidente Masaryk 111, Polanco, Miguel Hidalgo, CDMX, 11560",
                    "LP-HOG-34110", "Licuadora de vaso", 1, "1899.00",
                    OrderChannel.WEBSITE, OrderStatus.CANCELLED, 9),
            new Seed("andrea.nunez@correo.com",
                    "Francisco Sosa 120, Santa Catarina, Coyoacán, CDMX, 04010",
                    "LP-PER-22190", "Set de crema corporal", 3, "420.00",
                    OrderChannel.APP, OrderStatus.PREPARING, 2),
            new Seed("diego.paredes@correo.com",
                    "Av. Vasco de Quiroga 3800, Santa Fe, Cuajimalpa, CDMX, 05348",
                    "LP-ELC-79015", "Barra de sonido", 1, "3990.00",
                    OrderChannel.WEBSITE, OrderStatus.RECEIVED, 1),
            new Seed("fernanda.morales@correo.com",
                    "Av. Patria 1891, Jardines Universidad, Zapopan, Jalisco, 45110",
                    "LP-MOD-12110", "Zapatillas de piel", 1, "2590.00",
                    OrderChannel.APP, OrderStatus.CONFIRMED, 8),
            new Seed("jose.herrera@correo.com",
                    "Calle Orizaba 101, Roma Norte, Cuauhtémoc, CDMX, 06700",
                    "LP-HOG-35220", "Cafetera espresso", 1, "5490.00",
                    OrderChannel.WEBSITE, OrderStatus.SHIPPED, 6),
            new Seed("camila.delgado@correo.com",
                    "Blvd. Atlixco 1401, La Paz, Puebla, Puebla, 72160",
                    "LP-BEB-45088", "Monitor para bebé", 1, "2890.00",
                    OrderChannel.APP, OrderStatus.RECEIVED, 0)
    );

    private static final class Seed {
        private final String email;
        private final String address;
        private final String code;
        private final String description;
        private final int quantity;
        private final String price;
        private final OrderChannel channel;
        private final OrderStatus status;
        private final int daysAgo;

        private Seed(String email, String address, String code, String description, int quantity,
                     String price, OrderChannel channel, OrderStatus status, int daysAgo) {
            this.email = email;
            this.address = address;
            this.code = code;
            this.description = description;
            this.quantity = quantity;
            this.price = price;
            this.channel = channel;
            this.status = status;
            this.daysAgo = daysAgo;
        }

        private List<OrderStatus> trail() {
            if (status == OrderStatus.CANCELLED) {
                return Arrays.asList(OrderStatus.RECEIVED, OrderStatus.CANCELLED);
            }
            List<OrderStatus> flow = Arrays.asList(
                    OrderStatus.RECEIVED,
                    OrderStatus.CONFIRMED,
                    OrderStatus.PREPARING,
                    OrderStatus.SHIPPED,
                    OrderStatus.DELIVERED);
            return flow.subList(0, flow.indexOf(status) + 1);
        }
    }
}
