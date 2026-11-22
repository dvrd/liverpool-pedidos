package mx.com.liverpool.pedidos.comun;

import mx.com.liverpool.pedidos.web.error.RecursoNoEncontradoException;

import java.util.regex.Pattern;

public final class Identificadores {

    private static final Pattern OBJECT_ID = Pattern.compile("^[a-fA-F0-9]{24}$");

    private Identificadores() {
    }

    public static void exigir(String id, String mensajeSiFalta) {
        if (id == null || !OBJECT_ID.matcher(id).matches()) {
            throw new RecursoNoEncontradoException(mensajeSiFalta);
        }
    }
}
