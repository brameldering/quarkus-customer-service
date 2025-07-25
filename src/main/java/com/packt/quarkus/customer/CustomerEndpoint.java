package com.packt.quarkus.customer;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonString;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional; // Import for @Transactional

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/customers")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customer API", description = "Operations related to customer management.") // Updated description
public class CustomerEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerEndpoint.class);

    @Inject
    CustomerRepository repository;
    @Inject
    SecurityIdentity securityContext;

    @Inject
    JsonWebToken jwt;

    @Inject
    @Claim(standard = Claims.groups)
    Optional<JsonString> groups;

    @Inject
    @Claim(standard = Claims.preferred_username)
    Optional<JsonString> currentUsername;

    @GET
    @RolesAllowed({"user", "admin"})
    @Counted(description = "Customer list count", absolute = true)
    @Timed(name = "timerCheck", description = "How much time it takes to load the Customer list", unit = MetricUnits.MILLISECONDS)
    @Operation(operationId = "getAllCustomers", description = "Getting All customers")
    @APIResponse(responseCode = "200", description = "Successful response.")
    public List<Customer> getAll() {
        LOG.info("Received request to get all customers.");
        // Log currently logged in user information using securityContext from Quarkus
        LOG.info("Connected with User: {}", securityContext.getPrincipal().getName());
//        Iterator<String> roles = securityContext.getRoles().iterator();
//        while (roles.hasNext()) {
//            LOG.info("Role: "+roles.next());
//        }
        // Log JWT info
        LOG.info("========== JWT info ========");
        LOG.info("Username: {}", currentUsername);
        LOG.info("JWT To String: {}", jwt.toString());
        LOG.info("============================");

        List<Customer> customers = repository.findAll();
        LOG.debug("Found {} customers.", customers.size());
        return customers;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    @Path("/{id}") // Defines the path parameter for the ID
    @Operation(operationId = "getCustomerById", description = "Get a customer by id")
    @APIResponse(responseCode = "200", description = "Successful response.")
    @APIResponse(responseCode = "404", description = "Customer not found.")
    public Response getById(@Parameter(description = "The id of the customer to get.", required = true) @PathParam("id") Long id) {
        LOG.info("Received request to get customer by ID: {}", id);
        Customer customer = repository.findCustomerById(id);
        if (customer != null) {
            LOG.debug("Found customer with ID: {}", id);
            return Response.ok(customer).build(); // Return 200 OK with the customer object
        } else {
            LOG.warn("Customer with ID: {} not found.", id);
            return Response.status(Response.Status.NOT_FOUND).build(); // Return 404 Not Found
        }
    }

    @POST
    @RolesAllowed("admin")
    @Operation(operationId = "createCustomer", description = "Create a new customer")
    @APIResponse(responseCode = "201", description = "Successfully created" )
    @Transactional // Ensure this method runs within a transaction
    public Response create (@Parameter(description = "The new customer.", required = true) Customer customer) {
        LOG.info("Received request to create customer: {}", customer);
        try {
            repository.createCustomer(customer);
            LOG.info("Customer created successfully. ID: {}", customer.getId());
            // Return the created customer object in the response body
            // This is crucial for the test to extract the ID
            return Response.created(URI.create("/customers/" + customer.getId()))
                    .entity(customer) // Return the customer object
                    .build();
        } catch (Exception e) {
            LOG.error("Error creating customer: {}", customer, e);
            return Response.status(500).entity("Error creating customer").build();
        }
    }

    @PUT
    @RolesAllowed("admin")
    @Operation(operationId = "updateCustomer", description = "Update an existing customer")
    @APIResponse(responseCode = "200", description = "Successfully updated" )
    @Transactional
    public Response update(@Parameter(description = "The customer to update.", required = true) Customer customer) {
        LOG.info("Received request to update customer: {}", customer.getId());
        try {
            repository.updateCustomer(customer);
            LOG.info("Customer updated successfully: {}", customer.getId());
            return Response.status(204).build();
        } catch (Exception e) {
            LOG.error("Error updating customer: {}", customer.getId(), e);
            return Response.status(500).entity("Error updating customer").build();
        }
    }

    @DELETE
    @RolesAllowed("admin")
    @Path("/{id}") // Accept id as a path parameter
    @Operation(operationId = "deleteCustomer", description = "Delete a customer by ID")
    @APIResponse(responseCode = "204", description = "Successfully deleted" )
    @Transactional
    public Response delete(@Parameter(description = "The id of the customer to delete.", required = true) @PathParam("id") Long id) { // Changed to @PathParam
        LOG.info("Received request to delete customer with ID: {}", id);
        try {
            repository.deleteCustomer(id);
            LOG.info("Customer deleted successfully with ID: {}", id);
            return Response.status(204).build();
        } catch (Exception e) {
            LOG.error("Error deleting customer with ID: {}", id, e);
            return Response.status(500).entity("Error deleting customer").build();
        }
    }
}