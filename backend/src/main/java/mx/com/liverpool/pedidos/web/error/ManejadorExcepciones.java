package mx.com.liverpool.pedidos.web.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException ex, HttpServletRequest solicitud) {
        Map<String, String> detalles = new LinkedHashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            if (!detalles.containsKey(error.getField())) {
                detalles.put(error.getField(), error.getDefaultMessage());
            }
        });
        return ResponseEntity.badRequest().body(ErrorRespuesta.de(
                400,
                "Datos inválidos",
                "Revisa los campos marcados.",
                detalles,
                solicitud.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> ilegible(HttpMessageNotReadableException ex, HttpServletRequest solicitud) {
        return ResponseEntity.badRequest().body(ErrorRespuesta.de(
                400,
                "Solicitud ilegible",
                "El cuerpo no es válido. Revisa el formato JSON y los valores permitidos.",
                null,
                solicitud.getRequestURI()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest solicitud) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorRespuesta.de(
                404,
                "No encontrado",
                ex.getMessage(),
                null,
                solicitud.getRequestURI()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> regla(ReglaNegocioException ex, HttpServletRequest solicitud) {
        HttpStatus estatus = ex.getEstatus();
        return ResponseEntity.status(estatus).body(ErrorRespuesta.de(
                estatus.value(),
                estatus.getReasonPhrase(),
                ex.getMessage(),
                null,
                solicitud.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> general(Exception ex, HttpServletRequest solicitud) {
        log.error("Error no controlado en {}", solicitud.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorRespuesta.de(
                500,
                "Error interno",
                "Ocurrió un error inesperado.",
                null,
                solicitud.getRequestURI()));
    }
}
