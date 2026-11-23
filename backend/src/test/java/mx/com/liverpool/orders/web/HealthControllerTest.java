package mx.com.liverpool.orders.web;

import mx.com.liverpool.orders.web.error.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.dao.DataAccessResourceFailureException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthController.class)
@Import(ApiExceptionHandler.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MongoTemplate mongo;

    @Test
    void reportsOkWhenMongoResponds() throws Exception {
        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("order-desk"));
    }

    @Test
    void reportsAnInternalErrorWhenMongoDoesNotRespond() throws Exception {
        when(mongo.executeCommand(anyString())).thenThrow(new DataAccessResourceFailureException("down"));

        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("down"))
                .andExpect(jsonPath("$.service").value("order-desk"));
    }
}
