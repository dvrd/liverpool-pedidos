package mx.com.liverpool.orders.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.com.liverpool.orders.common.Text;

@Getter
@Setter
@NoArgsConstructor
public class CustomerSnapshot {

    private String firstName;
    private String paternalLastName;
    private String maternalLastName;
    private String email;

    public static CustomerSnapshot from(Customer customer) {
        CustomerSnapshot snapshot = new CustomerSnapshot();
        snapshot.setFirstName(customer.getFirstName());
        snapshot.setPaternalLastName(customer.getPaternalLastName());
        snapshot.setMaternalLastName(customer.getMaternalLastName());
        snapshot.setEmail(customer.getEmail());
        return snapshot;
    }

    public String fullName() {
        return Text.personName(firstName, paternalLastName, maternalLastName);
    }
}
