package mx.com.liverpool.pedidos.repositorio;

import mx.com.liverpool.pedidos.dominio.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PedidoRepository extends MongoRepository<Pedido, String> {

    boolean existsByUsuarioId(String usuarioId);
}
