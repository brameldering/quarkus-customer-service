package com.packt.quarkus.customer;

import com.packt.quarkus.order.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.util.List;

@Cacheable
@Entity
@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Generates a builder pattern
public class Customer extends PanacheEntityBase {
    @Id
    @SequenceGenerator(
            name = "customerSequence",
            sequenceName = "customerId_seq",
            allocationSize = 1,
            initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customerSequence")
    private Long id;

    @Column(length = 40)
    private String firstName;

    @Column(length = 40)
    private String lastName;

    @OneToMany(mappedBy = "customer")
    @JsonbTransient
    public List<OrderDetail> orderDetails;
}

// example using lombok builder
//Customer customer = Customer.builder()
//        .firstName("Bram")
//        .id(1)
//        .lastName("Eldering")
//        .build();