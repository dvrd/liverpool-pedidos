package mx.com.liverpool.pedidos.web.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PaginaRespuesta<T> {

    private final List<T> contenido;
    private final int pagina;
    private final int tamano;
    private final long totalElementos;
    private final int totalPaginas;

    private PaginaRespuesta(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {
        this.contenido = contenido;
        this.pagina = pagina;
        this.tamano = tamano;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
    }

    public static <T> PaginaRespuesta<T> de(List<T> contenido, int pagina, int tamano, long totalElementos) {
        int totalPaginas = tamano == 0 ? 0 : (int) Math.ceil(totalElementos / (double) tamano);
        return new PaginaRespuesta<T>(contenido, pagina, tamano, totalElementos, totalPaginas);
    }
}
