package mx.com.liverpool.orders.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerResponse {

    private String firstName;
    private String paternalLastName;
    private String maternalLastName;
    private String fullName;
    private String email;
}
