package mx.com.liverpool.pedidos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Opción del typeahead. El valor es el término que filtra la lista al seleccionarse.")
public class SugerenciaRespuesta {

    private final String tipo;
    private final String etiqueta;
    private final String detalle;
    private final String valor;

    public SugerenciaRespuesta(String tipo, String etiqueta, String detalle, String valor) {
        this.tipo = tipo;
        this.etiqueta = etiqueta;
        this.detalle = detalle;
        this.valor = valor;
    }
}
