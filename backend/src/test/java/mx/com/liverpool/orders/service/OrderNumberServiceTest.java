package mx.com.liverpool.orders.service;

import mx.com.liverpool.orders.domain.Sequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderNumberServiceTest {

    @Mock
    private MongoTemplate mongo;

    @Test
    void formatsTheNextNumber() {
        Sequence sequence = new Sequence();
        sequence.setValue(7);
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class)))
                .thenReturn(sequence);

        assertThat(new OrderNumberService(mongo).next()).isEqualTo("LIV-000007");
    }

    @Test
    void doesNotInventANumberWhenTheSequenceDoesNotAdvance() {
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class)))
                .thenReturn(null);

        assertThatThrownBy(() -> new OrderNumberService(mongo).next())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void retriesOnceWhenTwoCreatesClaimTheCounterTogether() {
        Sequence sequence = new Sequence();
        sequence.setValue(8);
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class)))
                .thenThrow(new DuplicateKeyException("E11000"))
                .thenReturn(sequence);

        assertThat(new OrderNumberService(mongo).next()).isEqualTo("LIV-000008");
    }
}
