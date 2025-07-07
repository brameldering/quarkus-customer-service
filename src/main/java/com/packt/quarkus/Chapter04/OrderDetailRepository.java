package com.packt.quarkus.Chapter04;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@ApplicationScoped
public class OrderDetailRepository {

    private static final Logger LOG = LoggerFactory.getLogger(OrderDetailRepository.class);

    @Inject
    EntityManager entityManager;

    public List<OrderDetail> findAll(Long customerId) {
        TypedQuery<OrderDetail> query = entityManager.createNamedQuery("OrderDetails.findAll", OrderDetail.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    public OrderDetail findOrderById(Long id) {
        OrderDetail orderDetail = entityManager.find(OrderDetail.class, id);
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
        entityManager.persist(orderDetail);
        // After persist, the 'id' of orderDetail is automatically populated by the database
        LOG.info("OrderDetail with ID: " + orderDetail.getId() + " created successfully. ID: ");
        return orderDetail;
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        OrderDetail o = findOrderById(orderId);
        entityManager.remove(o);
    }

}
