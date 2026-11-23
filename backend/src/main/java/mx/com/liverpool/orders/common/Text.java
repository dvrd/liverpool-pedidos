package mx.com.liverpool.orders.common;

import mx.com.liverpool.orders.domain.CustomerSnapshot;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.OrderChannel;
import mx.com.liverpool.orders.domain.OrderStatus;

import java.text.Normalizer;
import java.util.Locale;

public final class Text {

    private Text() {
    }

    public static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    public static String email(String value) {
        return clean(value).toLowerCase(Locale.ROOT);
    }

    public static String personName(String firstName, String paternalLastName, String maternalLastName) {
        return (empty(firstName) + " " + empty(paternalLastName) + " " + empty(maternalLastName))
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String normalize(String value) {
        String base = clean(value);
        if (base.isEmpty()) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    /**
     * MongoDB evaluates the pattern with PCRE. {@code Pattern.quote} uses \\Q and \\E,
     * which PCRE does not treat as a literal quote, so each metacharacter is escaped.
     */
    public static String literalRegex(String value) {
        return normalize(value).replaceAll("([\\\\.^$|?*+()\\[\\]{}])", "\\\\$1");
    }

    public static String searchIndex(Order order) {
        String channel = channelWords(order.getChannel());
        CustomerSnapshot customer = order.getCustomer();
        String customerData = customer == null ? "" : String.join(" ",
                empty(customer.getFirstName()),
                empty(customer.getPaternalLastName()),
                empty(customer.getMaternalLastName()),
                empty(customer.getEmail()));
        return normalize(String.join(" ",
                empty(order.getOrderNumber()),
                customerData,
                empty(order.getShippingAddress()),
                empty(order.getProductCode()),
                empty(order.getProductDescription()),
                channel,
                statusWords(order.getStatus())));
    }

    private static String statusWords(OrderStatus status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case RECEIVED:
                return "recibido";
            case CONFIRMED:
                return "confirmado";
            case PREPARING:
                return "en preparacion";
            case SHIPPED:
                return "enviado";
            case DELIVERED:
                return "entregado";
            case CANCELLED:
                return "cancelado";
            default:
                throw new IllegalStateException("Missing search words for " + status);
        }
    }

    private static String channelWords(OrderChannel channel) {
        if (channel == OrderChannel.APP) {
            return "aplicacion app";
        }
        if (channel == OrderChannel.WEBSITE) {
            return "sitio web";
        }
        return "";
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }
}
