package mx.com.liverpool.orders.web;

import mx.com.liverpool.orders.domain.OrderStatus;
import mx.com.liverpool.orders.domain.SuggestionType;
import mx.com.liverpool.orders.service.OrderService;
import mx.com.liverpool.orders.web.dto.OrderResponse;
import mx.com.liverpool.orders.web.dto.SuggestionResponse;
import mx.com.liverpool.orders.web.error.ApiExceptionHandler;
import mx.com.liverpool.orders.web.error.BusinessRuleException;
import mx.com.liverpool.orders.web.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import(ApiExceptionHandler.class)
class OrderControllerTest {

    private static final String ID = "507f1f77bcf86cd799439011";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrderService orders;

    @Test
    void rejectsAnOrderWithoutData() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Revisa los campos marcados."))
                .andExpect(jsonPath("$.details.customerId").value("El cliente es obligatorio"))
                .andExpect(jsonPath("$.details.shippingAddress").value("La dirección de envío es obligatoria"))
                .andExpect(jsonPath("$.details.unitPrice").value("El precio es obligatorio"));

        verifyNoInteractions(orders);
    }

    @Test
    void rejectsABodyThatIsNotJson() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo no es válido. Revisa el formato JSON y los valores permitidos."));

        verifyNoInteractions(orders);
    }

    @Test
    void createsTheOrder() throws Exception {
        OrderResponse created = new OrderResponse();
        created.setId(ID);
        created.setOrderNumber("LIV-000015");
        when(orders.create(any())).thenReturn(created);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + ID + "\",\"shippingAddress\":\"Masaryk 111\","
                                + "\"productCode\":\"LP-MOD-10442\",\"productDescription\":\"Vestido\","
                                + "\"quantity\":1,\"unitPrice\":1890.00,\"channel\":\"WEBSITE\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/" + ID)))
                .andExpect(jsonPath("$.orderNumber").value("LIV-000015"));
    }

    @Test
    void changesTheStatus() throws Exception {
        OrderResponse updated = new OrderResponse();
        updated.setId(ID);
        updated.setStatus(OrderStatus.SHIPPED);
        when(orders.changeStatus(ID, OrderStatus.SHIPPED)).thenReturn(updated);

        mvc.perform(patch("/api/v1/orders/" + ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void deletesTheOrder() throws Exception {
        mvc.perform(delete("/api/v1/orders/" + ID))
                .andExpect(status().isNoContent());

        verify(orders).delete(ID);
    }

    @Test
    void returnsSuggestions() throws Exception {
        when(orders.suggest("jose")).thenReturn(Collections.singletonList(
                new SuggestionResponse(SuggestionType.CUSTOMER, "José Luis Herrera Díaz", "LIV-000002", "José Luis Herrera Díaz")));

        mvc.perform(get("/api/v1/orders/suggestions").param("term", "jose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CUSTOMER"))
                .andExpect(jsonPath("$[0].label").value("José Luis Herrera Díaz"));
    }

    @Test
    void mapsANegativePageToTheSpanishMessage() throws Exception {
        when(orders.list(isNull(), isNull(), eq(-1), eq(20)))
                .thenThrow(new BusinessRuleException(HttpStatus.BAD_REQUEST, "La página no puede ser negativa."));

        mvc.perform(get("/api/v1/orders").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.message").value("La página no puede ser negativa."));
    }

    @Test
    void rejectsAPageThatIsNotANumber() throws Exception {
        mvc.perform(get("/api/v1/orders").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El valor enviado no es válido."));

        verifyNoInteractions(orders);
    }

    @Test
    void rejectsAnUnsupportedMethodInSpanish() throws Exception {
        mvc.perform(patch("/api/v1/orders"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("Método no permitido"))
                .andExpect(jsonPath("$.message").value("Ese método no está permitido."));

        verifyNoInteractions(orders);
    }

    @Test
    void rejectsAnUnsupportedMediaTypeInSpanish() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("Formato no admitido"))
                .andExpect(jsonPath("$.message").value("El formato enviado no es válido."));

        verifyNoInteractions(orders);
    }

    @Test
    void aNonHexIdUsesTheSpanishNotFound() throws Exception {
        when(orders.get("no-es-un-id")).thenThrow(new NotFoundException("Pedido no encontrado."));

        mvc.perform(get("/api/v1/orders/no-es-un-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido no encontrado."));
    }
}
