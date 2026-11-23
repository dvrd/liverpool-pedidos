package mx.com.liverpool.orders.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final Map<String, String> details;
    private final String path;
    private final Instant timestamp;

    private ErrorResponse(int status, String error, String message, Map<String, String> details, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public static ErrorResponse of(int status, String error, String message, Map<String, String> details, String path) {
        return new ErrorResponse(status, error, message, details, path);
    }
}
