package mx.com.liverpool.pedidos.dominio;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class EventoEstatus {

    private EstatusPedido estatus;
    private Instant ocurridoEn;

    public static EventoEstatus de(EstatusPedido estatus, Instant ocurridoEn) {
        EventoEstatus evento = new EventoEstatus();
        evento.setEstatus(estatus);
        evento.setOcurridoEn(ocurridoEn);
        return evento;
    }
}
