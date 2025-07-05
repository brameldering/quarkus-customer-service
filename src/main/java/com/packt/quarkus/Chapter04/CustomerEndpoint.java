package com.packt.quarkus.Chapter04;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/customers")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerEndpoint.class);

    @Inject
    CustomerRepository repository;

    @GET
    public List<Customer> getAll() {
        LOG.info("Received request to get all customers.");
        List<Customer> customers = repository.findAll();
        LOG.debug("Found " + customers.size() + " customers.");
        return customers;
    }

    @POST
    public Response create (Customer customer) {
        LOG.info("Received request to create customer: " + customer); // Be careful logging sensitive data
        try {
            repository.createCustomer(customer);
            LOG.info("Customer created successfully.");
            return Response.status(201).build();
        } catch (Exception e) {
            LOG.error("Error creating customer: " + customer, e);
            // You might want to return a different response status code in case of error
            return Response.status(500).entity("Error creating customer").build();
        }
    }

    @PUT
    public Response update(Customer customer) {
        LOG.info("Received request to update customer: " + customer.getId());
        try {
            repository.updateCustomer(customer);
            LOG.info("Customer updated successfully: " + customer.getId());
            return Response.status(204).build();
        } catch (Exception e) {
            LOG.error("Error updating customer: " + customer.getId(), e);
            return Response.status(500).entity("Error updating customer").build();
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") Integer customerId) {
        LOG.info("Received request to delete customer with ID: " + customerId);
        try {
            repository.deleteCustomer(customerId);
            LOG.info("Customer deleted successfully with ID: " + customerId);
            return Response.status(204).build();
        } catch (Exception e) {
            LOG.error("Error deleting customer with ID: " + customerId, e);
            return Response.status(500).entity("Error deleting customer").build();
        }
    }
}
