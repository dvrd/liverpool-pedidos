package mx.com.liverpool.pedidos.dominio;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "secuencias")
public class Secuencia {

    @Id
    private String id;
    private long valor;
}
