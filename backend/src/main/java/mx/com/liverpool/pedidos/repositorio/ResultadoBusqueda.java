package mx.com.liverpool.pedidos.repositorio;

import mx.com.liverpool.pedidos.dominio.Pedido;

import java.util.List;

public class ResultadoBusqueda {

    private final List<Pedido> pedidos;
    private final long total;

    public ResultadoBusqueda(List<Pedido> pedidos, long total) {
        this.pedidos = pedidos;
        this.total = total;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public long getTotal() {
        return total;
    }
}
