package mx.com.liverpool.pedidos.servicio;

import mx.com.liverpool.pedidos.dominio.CanalPedido;
import mx.com.liverpool.pedidos.dominio.EstatusPedido;
import mx.com.liverpool.pedidos.dominio.Pedido;
import mx.com.liverpool.pedidos.dominio.Usuario;
import mx.com.liverpool.pedidos.repositorio.PedidoBusqueda;
import mx.com.liverpool.pedidos.repositorio.PedidoRepository;
import mx.com.liverpool.pedidos.repositorio.UsuarioRepository;
import mx.com.liverpool.pedidos.servicio.impl.PedidoServiceImpl;
import mx.com.liverpool.pedidos.servicio.impl.UsuarioServiceImpl;
import mx.com.liverpool.pedidos.web.dto.CrearPedidoSolicitud;
import mx.com.liverpool.pedidos.web.dto.PedidoRespuesta;
import mx.com.liverpool.pedidos.web.error.ReglaNegocioException;
import mx.com.liverpool.pedidos.web.mapeo.PedidoMapper;
import mx.com.liverpool.pedidos.web.mapeo.UsuarioMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidos;
    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private PedidoBusqueda busqueda;
    @Mock
    private FolioService folios;

    @Test
    void crearCopiaAlClienteCalculaElFolioYElImporte() {
        Usuario usuario = new Usuario();
        usuario.setId("507f1f77bcf86cd799439011");
        usuario.setNombre("María");
        usuario.setApellidoPaterno("Núñez");
        usuario.setApellidoMaterno("Solís");
        usuario.setCorreoElectronico("maria.nunez@correo.com");
        when(usuarios.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(folios.siguiente()).thenReturn("LIV-000099");
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId("507f1f77bcf86cd799439012");
            return pedido;
        });

        CrearPedidoSolicitud solicitud = new CrearPedidoSolicitud();
        solicitud.setUsuarioId(usuario.getId());
        solicitud.setDireccionEnvio("Francisco Sosa 120, Coyoacán");
        solicitud.setCodigoProducto("lp-per-22018");
        solicitud.setDescripcionProducto("Eau de parfum");
        solicitud.setCantidad(2);
        solicitud.setPrecio(new BigDecimal("1650"));
        solicitud.setCanal(CanalPedido.SITIO_WEB);

        PedidoServiceImpl servicio = servicio();
        PedidoRespuesta respuesta = servicio.crear(solicitud);

        assertThat(respuesta.getFolio()).isEqualTo("LIV-000099");
        assertThat(respuesta.getImporteTotal()).isEqualByComparingTo("3300.00");
        assertThat(respuesta.getCliente().getCorreoElectronico()).isEqualTo("maria.nunez@correo.com");
        assertThat(respuesta.getEstatus()).isEqualTo(EstatusPedido.RECIBIDO);
        assertThat(respuesta.getCodigoProducto()).isEqualTo("LP-PER-22018");
        assertThat(respuesta.getHistorial()).hasSize(1);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidos).save(captor.capture());
        assertThat(captor.getValue().getTextoBusqueda()).contains("maria nunez");
    }

    @Test
    void noEliminaUnClienteConPedidos() {
        Usuario usuario = new Usuario();
        usuario.setId("507f1f77bcf86cd799439011");
        when(usuarios.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(pedidos.existsByUsuarioId(usuario.getId())).thenReturn(true);

        UsuarioServiceImpl servicio = new UsuarioServiceImpl(usuarios, pedidos, new UsuarioMapper());

        assertThatThrownBy(() -> servicio.eliminar(usuario.getId()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pedidos asociados");
    }

    private PedidoServiceImpl servicio() {
        return new PedidoServiceImpl(pedidos, usuarios, busqueda, folios, new ArmadorSugerencias(), new PedidoMapper());
    }
}
