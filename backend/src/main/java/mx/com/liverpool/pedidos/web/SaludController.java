package mx.com.liverpool.pedidos.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/salud")
@Tag(name = "Salud", description = "Comprueba que el servicio y MongoDB responden")
public class SaludController {

    private final MongoTemplate mongo;

    public SaludController(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @GetMapping
    @Operation(summary = "Estado del servicio y de la base")
    public Map<String, String> salud() {
        mongo.executeCommand("{ ping: 1 }");
        Map<String, String> cuerpo = new LinkedHashMap<String, String>();
        cuerpo.put("estado", "ok");
        cuerpo.put("servicio", "mesa-de-pedidos");
        return cuerpo;
    }
}
