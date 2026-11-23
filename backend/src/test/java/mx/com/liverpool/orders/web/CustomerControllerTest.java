package mx.com.liverpool.orders.web;

import mx.com.liverpool.orders.service.CustomerService;
import mx.com.liverpool.orders.web.dto.CustomerRecord;
import mx.com.liverpool.orders.web.error.ApiExceptionHandler;
import mx.com.liverpool.orders.web.error.BusinessRuleException;
import mx.com.liverpool.orders.web.error.NotFoundException;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerController.class)
@Import(ApiExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CustomerService customers;

    @Test
    void rejectsACustomerWithoutData() throws Exception {
        mvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.firstName").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.details.email").value("El correo electrónico es obligatorio"));

        verifyNoInteractions(customers);
    }

    @Test
    void responds404WhenTheCustomerDoesNotExist() throws Exception {
        when(customers.get("507f1f77bcf86cd799439011"))
                .thenThrow(new NotFoundException("Cliente no encontrado."));

        mvc.perform(get("/api/v1/customers/507f1f77bcf86cd799439011"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado."));
    }

    @Test
    void createsTheCustomer() throws Exception {
        CustomerRecord created = new CustomerRecord();
        created.setId("507f1f77bcf86cd799439011");
        created.setFirstName("Mariana");
        created.setEmail("mariana.castillo@correo.com");
        when(customers.create(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        mvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Mariana\",\"paternalLastName\":\"Castillo\","
                                + "\"maternalLastName\":\"Reyes\",\"email\":\"mariana.castillo@correo.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("507f1f77bcf86cd799439011"))
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/507f1f77bcf86cd799439011")));
    }

    @Test
    void refusesToDeleteACustomerWhoHasOrders() throws Exception {
        doThrow(new BusinessRuleException(HttpStatus.CONFLICT, "No se puede eliminar el cliente porque tiene pedidos asociados."))
                .when(customers).delete("507f1f77bcf86cd799439011");

        mvc.perform(delete("/api/v1/customers/507f1f77bcf86cd799439011"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflicto"))
                .andExpect(jsonPath("$.message").value("No se puede eliminar el cliente porque tiene pedidos asociados."));
    }
}
