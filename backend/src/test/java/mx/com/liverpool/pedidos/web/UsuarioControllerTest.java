package mx.com.liverpool.pedidos.web;

import mx.com.liverpool.pedidos.servicio.UsuarioService;
import mx.com.liverpool.pedidos.web.dto.UsuarioRespuesta;
import mx.com.liverpool.pedidos.web.error.ManejadorExcepciones;
import mx.com.liverpool.pedidos.web.error.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
@Import(ManejadorExcepciones.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UsuarioService usuarios;

    @Test
    void rechazaUnClienteSinDatos() throws Exception {
        mvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.detalles.correoElectronico").value("El correo electrónico es obligatorio"));

        verifyNoInteractions(usuarios);
    }

    @Test
    void responde404CuandoElClienteNoExiste() throws Exception {
        when(usuarios.obtener("507f1f77bcf86cd799439011"))
                .thenThrow(new RecursoNoEncontradoException("Cliente no encontrado."));

        mvc.perform(get("/api/v1/usuarios/507f1f77bcf86cd799439011"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado."));
    }

    @Test
    void creaElCliente() throws Exception {
        UsuarioRespuesta creado = new UsuarioRespuesta();
        creado.setId("507f1f77bcf86cd799439011");
        creado.setNombre("Mariana");
        creado.setCorreoElectronico("mariana.castillo@correo.com");
        when(usuarios.crear(org.mockito.ArgumentMatchers.any())).thenReturn(creado);

        mvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Mariana\",\"apellidoPaterno\":\"Castillo\","
                                + "\"apellidoMaterno\":\"Reyes\",\"correoElectronico\":\"mariana.castillo@correo.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("507f1f77bcf86cd799439011"));
    }
}
