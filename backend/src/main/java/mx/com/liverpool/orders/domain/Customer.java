package mx.com.liverpool.orders.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.orders.common.Text;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "customers")
public class Customer {

    @Id
    private String id;
    private String firstName;
    private String paternalLastName;
    private String maternalLastName;
    @Indexed(unique = true)
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    public String fullName() {
        return Text.personName(firstName, paternalLastName, maternalLastName);
    }
}
