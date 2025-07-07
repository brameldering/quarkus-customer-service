package com.packt.quarkus.Chapter04;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import java.util.List;

@Cacheable
@Entity
@NamedQuery(name = "Customers.findAll",
        query = "SELECT c FROM Customer c ORDER BY c.id",
        hints = @QueryHint(name = "org.hibernate.cacheable", value = "true") )
@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Generates a builder pattern
public class Customer {
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

    public String getFullName() {
        return firstName + " " + lastName;
    }

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