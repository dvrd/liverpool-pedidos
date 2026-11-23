package mx.com.liverpool.orders.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Salud", description = "Comprueba que el servicio y MongoDB responden")
public class HealthController {

    private final MongoTemplate mongo;

    public HealthController(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @GetMapping
    @Operation(summary = "Estado del servicio y de la base")
    public ResponseEntity<Map<String, String>> health() {
        try {
            mongo.executeCommand("{ ping: 1 }");
            return ResponseEntity.ok(body("ok"));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body("down"));
        }
    }

    private Map<String, String> body(String status) {
        Map<String, String> body = new LinkedHashMap<String, String>();
        body.put("status", status);
        body.put("service", "order-desk");
        return body;
    }
}
