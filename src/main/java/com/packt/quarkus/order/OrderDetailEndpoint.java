package com.packt.quarkus.order;

import com.packt.quarkus.customer.Customer;
import com.packt.quarkus.customer.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

@Path("/orders")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "OrderDetail API", description = "Bladiebla")
public class OrderDetailEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(OrderDetailEndpoint.class);

    @Inject
    OrderDetailRepository orderDetailRepository;
    @Inject
    CustomerRepository customerRepository;

    @Context
    UriInfo uriInfo;

    @GET
    @Operation(operationId = "all", description = "Getting All orders")
    @APIResponse(responseCode = "200", description = "Successful response.")
    public List<OrderDetail> getAll(@QueryParam("customerId") Long customerId) {
        LOG.info("Received request to get all orders.");
        List<OrderDetail> orders = orderDetailRepository.findAllForCustomer(customerId);
        LOG.debug("Found " + orders.size() + " orders.");
        return orders;
    }

    @GET
    @Path("/{id}") // Path parameter for the order detail ID
    @Operation(operationId = "id", description = "Get an order by id")
    @APIResponse(responseCode = "200", description = "Successful response.")
    @APIResponse(responseCode = "404", description = "OrderDetail not found.")
    public Response getById(@PathParam("id") Long id) {
        LOG.info("Received request to get OrderDetail with ID: " + id);
        try {
            OrderDetail orderDetail = orderDetailRepository.findOrderById(id);
            LOG.info("Found OrderDetail with ID: " + id);
            return Response.ok(orderDetail).build();
        } catch (WebApplicationException e) {
            // findOrderById throws WebApplicationException with 404 if not found
            LOG.warn("OrderDetail with ID " + id + " not found: " + e.getMessage());
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Error retrieving OrderDetail with ID: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving OrderDetail: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/{customerId}")
    @Operation(operationId = "OrderDetail", description = "Create an order")
    @APIResponse(responseCode = "201", description = "Successfully created" )
    public Response create (@Parameter(description = "The new order detail.", required = true) OrderDetail orderDetail, @Parameter(description = "The customer id for which to create the order.", required = true) @PathParam("customerId") Long customerId) {
        LOG.info("Received request to create OrderDetail: " + orderDetail + " for customer ID: " + customerId);
        try {
            Customer customer = customerRepository.findCustomerById(customerId);
            if (customer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Customer with id " + customerId + " not found.")
                        .build();
            }
            OrderDetail createdOrderDetail = orderDetailRepository.createOrder(orderDetail, customer);

            // Construct the URI for the newly created resource
            // Assuming a path like /orders/{id} for fetching a specific order detail
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(createdOrderDetail.getId().toString())
                    .build();

            LOG.info("OrderDetail created successfully with ID: " + createdOrderDetail.getId());

            // Return 201 Created, with the Location header and the created entity in the body
            return Response.created(location)
                    .entity(createdOrderDetail)
                    .build();
        } catch (WebApplicationException e) {
            LOG.error("Error creating OrderDetail: Customer not found.", e);
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Error creating OrderDetail: " + orderDetail, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating OrderDetail: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Operation(operationId = "OrderDetail", description = "Update an OrderDetail")
    @APIResponse(responseCode = "200", description = "Successfully updated" )
    public Response update(@Parameter(description = "The order detail to update.", required = true) OrderDetail order) {
        LOG.info("Received request to update OrderDetail: " + order.getId());
        try {
            orderDetailRepository.updateOrder(order);
            LOG.info("OrderDetail updated successfully: " + order.getId());
            return Response.status(204).build();
        } catch (Exception e) {
            LOG.error("Error updating OrderDetail: " + order.getId(), e);
            return Response.status(500).entity("Error updating OrderDetail").build();
        }
    }

    @DELETE
    @Path("/{orderId}") // Renamed path parameter for clarity
    @Operation(operationId = "id", description = "Delete an order detail")
    @APIResponse(responseCode = "204", description = "Successfully deleted" )
    public Response delete(@Parameter(description = "The id of the order detail to delete.", required = true) @PathParam("orderId") Long orderId) {
        LOG.info("Received request to delete OrderDetail with ID: " + orderId);
        try {
            orderDetailRepository.deleteOrder(orderId);
            LOG.info("OrderDetail deleted successfully with ID: " + orderId);
            return Response.status(204).build();
        } catch (WebApplicationException e) {
            LOG.warn("Error deleting OrderDetail: " + e.getMessage());
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Error deleting OrderDetail with ID: " + orderId, e);
            return Response.status(500).entity("Error deleting OrderDetail").build();
        }
    }
}