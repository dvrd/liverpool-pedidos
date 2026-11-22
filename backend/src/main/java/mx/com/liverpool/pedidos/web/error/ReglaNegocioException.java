package mx.com.liverpool.pedidos.web.error;

import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus estatus;

    public ReglaNegocioException(HttpStatus estatus, String mensaje) {
        super(mensaje);
        this.estatus = estatus;
    }

    public HttpStatus getEstatus() {
        return estatus;
    }
}
