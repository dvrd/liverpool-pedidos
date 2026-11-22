package mx.com.liverpool.pedidos.repositorio;

import mx.com.liverpool.pedidos.comun.Textos;
import mx.com.liverpool.pedidos.dominio.EstatusPedido;
import mx.com.liverpool.pedidos.dominio.Pedido;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PedidoBusqueda {

    private final MongoTemplate mongo;

    public PedidoBusqueda(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public ResultadoBusqueda buscar(String termino, EstatusPedido estatus, int pagina, int tamano) {
        Criteria criterios = criterios(termino, estatus);
        Query conteo = new Query(criterios);
        long total = mongo.count(conteo, Pedido.class);

        Query paginaActual = new Query(criterios);
        paginaActual.with(Sort.by(Sort.Order.desc("creadoEn"), Sort.Order.desc("folio")));
        paginaActual.skip((long) pagina * tamano);
        paginaActual.limit(tamano);
        return new ResultadoBusqueda(mongo.find(paginaActual, Pedido.class), total);
    }

    public List<Pedido> coincidencias(String termino, int limite) {
        Query query = new Query(criterios(termino, null));
        query.with(Sort.by(Sort.Order.desc("creadoEn")));
        query.limit(limite);
        return mongo.find(query, Pedido.class);
    }

    private Criteria criterios(String termino, EstatusPedido estatus) {
        List<Criteria> partes = new ArrayList<Criteria>();
        String aguja = Textos.regexLiteral(termino);
        if (!aguja.isEmpty()) {
            partes.add(Criteria.where("textoBusqueda").regex(aguja, "i"));
        }
        if (estatus != null) {
            partes.add(Criteria.where("estatus").is(estatus));
        }
        if (partes.isEmpty()) {
            return new Criteria();
        }
        if (partes.size() == 1) {
            return partes.get(0);
        }
        return new Criteria().andOperator(partes.toArray(new Criteria[0]));
    }
}
