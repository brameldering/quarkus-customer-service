package com.packt.quarkus.customer;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class StaticCustomerListHandler implements FallbackHandler<List<Customer>> {

    private static final Logger LOG = LoggerFactory.getLogger(StaticCustomerListHandler.class);

    @Override
    public List<Customer> handle(ExecutionContext context) {
        LOG.warn("Executing FallbackHandler due to: " + context.getFailure().getClass().getSimpleName());
        LOG.info("Building Static List of Customers from Fallback Handler.");
        return buildStaticList();
    }

    private static List<Customer> buildStaticList() {
        List<Customer> customerList = new ArrayList<>();
        Customer c1 = new Customer();
        c1.setId(1L);
        c1.setFirstName("John");
        c1.setLastName("Doe");

        Customer c2 = new Customer();
        c2.setId(2L);
        c2.setFirstName("Fred");
        c2.setLastName("Smith");

        customerList.add(c1);
        customerList.add(c2);
        return customerList;
    }
}
