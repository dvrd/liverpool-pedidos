package mx.com.liverpool.orders.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import mx.com.liverpool.orders.domain.SuggestionType;

@Getter
@Schema(description = "Opción del typeahead. El valor es el término que filtra la lista al seleccionarse.")
public class SuggestionResponse {

    private final SuggestionType type;
    private final String label;
    private final String detail;
    private final String value;

    public SuggestionResponse(SuggestionType type, String label, String detail, String value) {
        this.type = type;
        this.label = label;
        this.detail = detail;
        this.value = value;
    }
}
