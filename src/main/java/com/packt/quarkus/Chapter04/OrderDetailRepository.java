package com.packt.quarkus.Chapter04;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.WebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@ApplicationScoped
public class OrderDetailRepository {

    private static final Logger LOG = LoggerFactory.getLogger(OrderDetailRepository.class);

    public List<OrderDetail> findAll(Long customerId) {
        return OrderDetail.list("customer.id", Sort.by("item"), customerId);
    }

    public OrderDetail findOrderById(Long id) {
        OrderDetail orderDetail = OrderDetail.findById(id);
        if (orderDetail == null) {
            throw new WebApplicationException("OrderDetail with id of " + id + " does not exist.", 404);
        }
        return orderDetail;
    }

    @Transactional
    public void updateOrder(OrderDetail order) {
        OrderDetail orderToUpdate = findOrderById(order.getId());
        orderToUpdate.setItem(order.getItem());
        orderToUpdate.setPrice(order.getPrice());
    }

    @Transactional
    public OrderDetail createOrder(OrderDetail orderDetail, Customer customer) {
        orderDetail.setCustomer(customer); // Link the order detail to the customer
        orderDetail.persist();
        // After persist, the 'id' of orderDetail is automatically populated by the database
        LOG.info("OrderDetail with ID: " + orderDetail.getId() + " created successfully.");
        return orderDetail;
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        OrderDetail orderDetail = findOrderById(orderId);
        orderDetail.delete();
    }
}
