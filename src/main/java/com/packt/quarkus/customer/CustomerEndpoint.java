package com.packt.quarkus.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional; // Import for @Transactional

import java.net.URI; // Import for URI.create
import java.util.List;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Timed;
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
    @Counted(description = "Customer list count", absolute = true)
    @Timed(name = "timerCheck", description = "How much time it takes to load the Customer list", unit = MetricUnits.MILLISECONDS)
    public List<Customer> getAll() {
        LOG.info("Received request to get all customers.");
        List<Customer> customers = repository.findAll();
        LOG.debug("Found " + customers.size() + " customers.");
        return customers;
    }

    // New method to get a single customer by ID
    @GET
    @Path("/{id}") // Defines the path parameter for the ID
    public Response getById(@PathParam("id") Long id) {
        LOG.info("Received request to get customer by ID: " + id);
        Customer customer = repository.findCustomerById(id); // Assuming this method exists in your repository
        if (customer != null) {
            LOG.debug("Found customer with ID: " + id);
            return Response.ok(customer).build(); // Return 200 OK with the customer object
        } else {
            LOG.warn("Customer with ID: " + id + " not found.");
            return Response.status(Response.Status.NOT_FOUND).build(); // Return 404 Not Found
        }
    }

    @POST
    @Transactional // Ensure this method runs within a transaction
    public Response create (Customer customer) {
        LOG.info("Received request to create customer: " + customer); // Be careful logging sensitive data
        try {
            repository.createCustomer(customer);
            LOG.info("Customer created successfully. ID: " + customer.getId());
            // Return the created customer object in the response body
            // This is crucial for the test to extract the ID
            return Response.created(URI.create("/customers/" + customer.getId()))
                    .entity(customer) // Return the customer object
                    .build();
        } catch (Exception e) {
            LOG.error("Error creating customer: " + customer, e);
            return Response.status(500).entity("Error creating customer").build();
        }
    }

    @PUT
    @Transactional // Ensure this method runs within a transaction
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
    @Path("/{id}") // Changed to accept id as a path parameter
    @Transactional // Ensure this method runs within a transaction
    public Response delete(@PathParam("id") Long id) { // Changed to @PathParam
        LOG.info("Received request to delete customer with ID: " + id);
        try {
            repository.deleteCustomer(id);
            LOG.info("Customer deleted successfully with ID: " + id);
            return Response.status(204).build();
        } catch (Exception e) {
            LOG.error("Error deleting customer with ID: " + id, e);
            return Response.status(500).entity("Error deleting customer").build();
        }
    }
}