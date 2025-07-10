package com.packt.quarkus.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import io.quarkus.panache.common.Sort;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class CustomerRepository {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerRepository.class);
    // This method will attempt to list all customers.
    // If it takes longer than 250ms, a TimeoutException will be thrown.
    // This TimeoutException will then trigger the FallbackHandler.
    @Timeout(250)
    @Fallback(value = StaticCustomerListHandler.class)
    @Retry(retryOn = {RuntimeException.class, TimeoutException.class}, maxRetries = 3)
    public List<Customer> findAll() {
        // Simulate a long-running operation or a database call
//        try {
            // For demonstration, let's make it occasionally timeout
//            long processingTime = System.currentTimeMillis() % 500 + 100; // Random time between 100-599ms
//            System.out.println("findAll() attempting to fetch data, expected time: " + processingTime + "ms");
//            TimeUnit.MILLISECONDS.sleep(processingTime);
            return Customer.listAll(Sort.by("id"));
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("Interrupted during data fetch", e);
//        }
    }

    public Customer findCustomerById(Long id) {
        Customer customer = Customer.findById(id);

        if (customer == null) {
            throw new WebApplicationException("Customer with id of " + id + " does not exist.", 404);
        }
        return customer;
    }

    @Transactional
    public void updateCustomer(Customer customer) {
        Customer customerToUpdate = findCustomerById(customer.getId());
        customerToUpdate.setFirstName(customer.getFirstName());
        customerToUpdate.setLastName(customer.getLastName());
    }

    @Transactional
    public void createCustomer(Customer customer) {
        customer.persist();
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = findCustomerById(customerId);
        customer.delete();
    }
}
