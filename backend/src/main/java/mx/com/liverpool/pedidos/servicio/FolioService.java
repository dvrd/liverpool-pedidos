package mx.com.liverpool.pedidos.servicio;

import mx.com.liverpool.pedidos.dominio.Secuencia;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class FolioService {

    static final String CLAVE = "pedido";

    private final MongoTemplate mongo;

    public FolioService(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    /**
     * Incremento atómico. Evita folios repetidos si dos altas ocurren a la vez,
     * sin exigir un replica set para una transacción de varios documentos.
     */
    public String siguiente() {
        Query query = Query.query(Criteria.where("_id").is(CLAVE));
        Update update = new Update().inc("valor", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);
        Secuencia secuencia = mongo.findAndModify(query, update, options, Secuencia.class);
        long valor = secuencia == null ? 1L : secuencia.getValor();
        return String.format("LIV-%06d", valor);
    }
}
