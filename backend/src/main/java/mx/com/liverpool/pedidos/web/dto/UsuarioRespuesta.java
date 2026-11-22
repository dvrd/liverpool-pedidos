package mx.com.liverpool.pedidos.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioRespuesta {

    private String id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String correoElectronico;
    private Instant creadoEn;
    private Instant actualizadoEn;
}
