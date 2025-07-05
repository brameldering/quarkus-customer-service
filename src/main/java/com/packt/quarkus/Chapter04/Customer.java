package com.packt.quarkus.Chapter04;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Generates a builder pattern
public class Customer {
    private Integer id;
    private String firstName;
    private String lastName;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

// example using lombok builder
//Customer customer = Customer.builder()
//        .firstName("Bram")
//        .id(1)
//        .lastName("Eldering")
//        .build();