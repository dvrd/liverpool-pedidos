package mx.com.liverpool.pedidos.dominio;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DatosCliente {

    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoElectronico;

    public static DatosCliente desde(Usuario usuario) {
        DatosCliente datos = new DatosCliente();
        datos.setNombre(usuario.getNombre());
        datos.setApellidoPaterno(usuario.getApellidoPaterno());
        datos.setApellidoMaterno(usuario.getApellidoMaterno());
        datos.setCorreoElectronico(usuario.getCorreoElectronico());
        return datos;
    }

    public String nombreCompleto() {
        return (valor(nombre) + " " + valor(apellidoPaterno) + " " + valor(apellidoMaterno))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }
}
