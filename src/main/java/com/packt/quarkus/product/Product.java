package com.packt.quarkus.product;

//import com.packt.quarkus.customer.Customer;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
//import jakarta.json.bind.annotation.JsonbTransient;
//import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Cacheable
//@Entity
@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Generates a builder pattern
//public class Product extends PanacheEntityBase {
public class Product {
//    @Id
//    @SequenceGenerator(
//            name = "productSequence",
//            sequenceName = "productId_seq",
//            allocationSize = 1,
//            initialValue = 1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "productSequence")
    private Long id;

//    @Column(length = 40)
    private String name;

//    @Column
    private Double price;

//    @ManyToOne
//    @JoinColumn(name = "order_id")
//    @JsonbTransient
//    public OrderDetail orderDetail;

}
