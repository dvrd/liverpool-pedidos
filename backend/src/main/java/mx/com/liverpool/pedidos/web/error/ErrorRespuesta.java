package mx.com.liverpool.pedidos.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorRespuesta {

    private final int estatus;
    private final String error;
    private final String mensaje;
    private final Map<String, String> detalles;
    private final String ruta;
    private final Instant marcaTiempo;

    private ErrorRespuesta(int estatus, String error, String mensaje, Map<String, String> detalles, String ruta) {
        this.estatus = estatus;
        this.error = error;
        this.mensaje = mensaje;
        this.detalles = detalles;
        this.ruta = ruta;
        this.marcaTiempo = Instant.now();
    }

    public static ErrorRespuesta de(int estatus, String error, String mensaje, Map<String, String> detalles, String ruta) {
        return new ErrorRespuesta(estatus, error, mensaje, detalles, ruta);
    }
}
