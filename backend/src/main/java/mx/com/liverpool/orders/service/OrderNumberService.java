package mx.com.liverpool.orders.service;

import mx.com.liverpool.orders.domain.Sequence;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class OrderNumberService {

    static final String KEY = "order";

    private final MongoTemplate mongo;

    public OrderNumberService(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    /**
     * Atomic increment. Avoids duplicate numbers when two creates happen at once,
     * without requiring a replica set for a multi-document transaction.
     */
    public String next() {
        try {
            return advance();
        } catch (DuplicateKeyException first) {
            return advance();
        }
    }

    private String advance() {
        Query query = Query.query(Criteria.where("_id").is(KEY));
        Update update = new Update().inc("value", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);
        Sequence sequence = mongo.findAndModify(query, update, options, Sequence.class);
        if (sequence == null) {
            throw new IllegalStateException("The order sequence did not advance.");
        }
        return String.format("LIV-%06d", sequence.getValue());
    }
}
