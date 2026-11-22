package mx.com.liverpool.pedidos.dominio;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    @Indexed(unique = true)
    private String correoElectronico;
    private Instant creadoEn;
    private Instant actualizadoEn;
}
