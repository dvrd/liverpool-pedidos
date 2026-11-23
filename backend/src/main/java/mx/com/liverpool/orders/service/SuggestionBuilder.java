package mx.com.liverpool.orders.service;

import mx.com.liverpool.orders.common.Text;
import mx.com.liverpool.orders.domain.CustomerSnapshot;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.SuggestionType;
import mx.com.liverpool.orders.web.dto.SuggestionResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class SuggestionBuilder {

    static final int LIMIT = 8;

    public List<SuggestionResponse> build(String term, List<Order> orders) {
        String needle = Text.normalize(term);
        if (needle.length() < 2 || orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<SuggestionResponse> output = new ArrayList<SuggestionResponse>();
        Set<String> seen = new LinkedHashSet<String>();
        for (Order order : orders) {
            CustomerSnapshot customer = order.getCustomer();
            String name = customer == null ? "" : customer.fullName();
            String email = customer == null ? "" : customer.getEmail();
            consider(output, seen, SuggestionType.ORDER_NUMBER, order.getOrderNumber(), name, order.getOrderNumber(), needle);
            consider(output, seen, SuggestionType.CUSTOMER, name, join(order.getOrderNumber(), email), name, needle);
            consider(output, seen, SuggestionType.EMAIL, email, join(name, order.getOrderNumber()), email, needle);
            consider(output, seen, SuggestionType.PRODUCT, order.getProductCode(),
                    join(order.getProductDescription(), order.getOrderNumber()), order.getProductCode(), needle);
            if (!contains(order.getProductCode(), needle) && contains(order.getProductDescription(), needle)) {
                consider(output, seen, SuggestionType.PRODUCT, order.getProductDescription(),
                        join(order.getProductCode(), order.getOrderNumber()), order.getProductDescription(), needle);
            }
            if (output.size() >= LIMIT) {
                break;
            }
        }
        return output;
    }

    private void consider(List<SuggestionResponse> output, Set<String> seen, SuggestionType type,
                          String label, String detail, String value, String needle) {
        if (output.size() >= LIMIT || label == null || label.isEmpty() || value == null) {
            return;
        }
        if (!contains(label, needle) && !contains(value, needle)) {
            return;
        }
        if (!seen.add(type + "|" + Text.normalize(value))) {
            return;
        }
        output.add(new SuggestionResponse(type, label, detail, value));
    }

    private boolean contains(String text, String needle) {
        return Text.normalize(text).contains(needle);
    }

    private String join(String left, String right) {
        if (left == null || left.isEmpty()) {
            return right == null ? "" : right;
        }
        if (right == null || right.isEmpty()) {
            return left;
        }
        return left + " · " + right;
    }
}
