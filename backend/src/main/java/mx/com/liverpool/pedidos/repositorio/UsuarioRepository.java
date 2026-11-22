package mx.com.liverpool.pedidos.repositorio;

import mx.com.liverpool.pedidos.dominio.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByCorreoElectronicoIgnoreCase(String correoElectronico);

    List<Usuario> findAllByOrderByNombreAscApellidoPaternoAsc();
}
