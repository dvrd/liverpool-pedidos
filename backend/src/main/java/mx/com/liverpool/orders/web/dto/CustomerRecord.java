package mx.com.liverpool.orders.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class CustomerRecord {

    private String id;
    private String firstName;
    private String paternalLastName;
    private String maternalLastName;
    private String fullName;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
}
