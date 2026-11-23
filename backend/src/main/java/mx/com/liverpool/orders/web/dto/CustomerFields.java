package mx.com.liverpool.orders.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public abstract class CustomerFields {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
    @Schema(example = "Mariana")
    private String firstName;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 80, message = "El apellido paterno admite hasta 80 caracteres")
    @Schema(example = "Castillo")
    private String paternalLastName;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(max = 80, message = "El apellido materno admite hasta 80 caracteres")
    @Schema(example = "Reyes")
    private String maternalLastName;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Size(max = 160, message = "El correo electrónico admite hasta 160 caracteres")
    @Schema(example = "mariana.castillo@correo.com")
    private String email;
}
