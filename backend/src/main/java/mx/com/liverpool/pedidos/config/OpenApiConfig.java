package mx.com.liverpool.pedidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mesaDePedidos() {
        return new OpenAPI().info(new Info()
                .title("Mesa de pedidos")
                .version("v1")
                .description("API REST para que el equipo de ventas gestione clientes y pedidos "
                        + "originados en la aplicación y en el sitio web. "
                        + "El importe del pedido se calcula como precio unitario por cantidad. "
                        + "El pedido conserva una copia de los datos del cliente al momento de crearse."));
    }
}
