package mx.com.liverpool.orders.web.dto;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void countsPagesFromTheTotal() {
        assertThat(PageResponse.of(Collections.emptyList(), 0, 50, 14).getTotalPages()).isEqualTo(1);
        assertThat(PageResponse.of(Collections.emptyList(), 1, 50, 51).getTotalPages()).isEqualTo(2);
        assertThat(PageResponse.of(Collections.emptyList(), 0, 20, 0).getTotalPages()).isZero();
    }
}
