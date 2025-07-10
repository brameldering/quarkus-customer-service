package com.packt.quarkus.order;

import com.packt.quarkus.customer.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@ApplicationScoped
public class OrderDetailRepository {

    private static final Logger LOG = LoggerFactory.getLogger(OrderDetailRepository.class);

    @CircuitBreaker(failOn={RuntimeException.class}, successThreshold = 5, requestVolumeThreshold = 4, failureRatio=0.75, delay = 1000)
    public List<OrderDetail> findAll(Long customerId) {
//        possibleFailure();
        return OrderDetail.list("customer.id", Sort.by("item"), customerId);
    }

    private void possibleFailure() {
        if (new Random().nextFloat() < 0.5f) {
            throw new RuntimeException("Resource failure.");
        }
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
        writeSomeLogging(orderDetail.getItem());
        return orderDetail;
    }

    // maximum 5 concurrent requests allowed, maximum 10 requests allowed in the waiting queue
    @Asynchronous
    @Bulkhead(value = 5, waitingTaskQueue = 10)
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public Future writeSomeLogging(String item) {
        LOG.info("New Customer order at: "+new java.util.Date());
        LOG.info("Item: {}", item);
        return CompletableFuture.completedFuture("ok");
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        OrderDetail orderDetail = findOrderById(orderId);
        orderDetail.delete();
    }
}
