package mx.com.liverpool.orders.common;

import mx.com.liverpool.orders.web.error.NotFoundException;

import java.util.regex.Pattern;

public final class Identifiers {

    private static final Pattern OBJECT_ID = Pattern.compile("^[a-fA-F0-9]{24}$");

    private Identifiers() {
    }

    public static void require(String id, String messageWhenMissing) {
        if (id == null || !OBJECT_ID.matcher(id).matches()) {
            throw new NotFoundException(messageWhenMissing);
        }
    }
}
