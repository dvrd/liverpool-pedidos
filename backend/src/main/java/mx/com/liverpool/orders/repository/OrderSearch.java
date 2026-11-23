package mx.com.liverpool.orders.repository;

import mx.com.liverpool.orders.common.Text;
import mx.com.liverpool.orders.domain.Order;
import mx.com.liverpool.orders.domain.OrderStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderSearch {

    private final MongoTemplate mongo;

    public OrderSearch(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public SearchResult search(String term, OrderStatus status, int page, int size) {
        Criteria criteria = criteria(term, status);
        long total = mongo.count(new Query(criteria), Order.class);

        Query pageQuery = new Query(criteria);
        pageQuery.with(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("orderNumber")));
        pageQuery.skip((long) page * size);
        pageQuery.limit(size);
        return new SearchResult(mongo.find(pageQuery, Order.class), total);
    }

    public List<Order> matches(String term, int limit) {
        Query query = new Query(criteria(term, null));
        query.with(Sort.by(Sort.Order.desc("createdAt")));
        query.limit(limit);
        return mongo.find(query, Order.class);
    }

    private Criteria criteria(String term, OrderStatus status) {
        List<Criteria> parts = new ArrayList<Criteria>();
        String needle = Text.literalRegex(term);
        if (!needle.isEmpty()) {
            parts.add(Criteria.where("searchText").regex(needle, "i"));
        }
        if (status != null) {
            parts.add(Criteria.where("status").is(status));
        }
        if (parts.isEmpty()) {
            return new Criteria();
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return new Criteria().andOperator(parts.toArray(new Criteria[0]));
    }
}
