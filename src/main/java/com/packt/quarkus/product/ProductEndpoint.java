package com.packt.quarkus.product;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Path("/products")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Product API", description = "Operations related to products.")
public class ProductEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ProductEndpoint.class);

    @Inject
    ProductRepository productRepository;

    @Context
    UriInfo uriInfo;

    @GET
    @RolesAllowed({"user", "admin"})
    @Operation(operationId = "getAllProducts", description = "Getting All products")
    @APIResponse(responseCode = "200", description = "Successful response.")
    public List<Product> getAll() {
        LOG.info("Received request to get all products.");
        List<Product> products = productRepository.findAll();
        LOG.debug("Found {} products.", products.size());
        return products;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    @Path("/{id}") // Path parameter for the product ID
    @Operation(operationId = "getProductById", description = "Get an product by id") 
    @APIResponse(responseCode = "200", description = "Successful response.")
    @APIResponse(responseCode = "404", description = "Product not found.")
    public Response getById(@PathParam("id") Long id) {
        LOG.info("Received request to get Product with ID: {}", id);
        try {
            Product product = productRepository.findProductById(id);
            LOG.info("Found Product with ID: {}", id);
            return Response.ok(product).build();
        } catch (WebApplicationException e) {
            LOG.warn("Product with ID {} not found: {}", id, e.getMessage());
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Error retrieving Product with ID: {}", id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving Product: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @RolesAllowed("admin")
    @Operation(operationId = "createProduct", description = "Create a new product")
    @APIResponse(responseCode = "201", description = "Successfully created" )
    public Response create (@Parameter(description = "The new product.", required = true) Product product) {
        LOG.info("Received request to create Product: {}", product);
        try {
            Product createdProduct = productRepository.createProduct(product);

            // Construct the URI for the newly created resource
            // Assuming a path like /products/{id} for fetching a specific product
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(createdProduct.getId().toString())
                    .build();

            LOG.info("Product created successfully with ID: {}", createdProduct.getId());

            // Return 201 Created, with the Location header and the created entity in the body
            return Response.created(location)
                    .entity(createdProduct)
                    .build();
        } catch (Exception e) {
            LOG.error("Error creating Product: {}", product, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating Product: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @RolesAllowed("admin")
    @Operation(operationId = "updateProduct", description = "Update an existing Product") 
    @APIResponse(responseCode = "200", description = "Successfully updated" )
    public Response update(@Parameter(description = "The product to update.", required = true) Product product) {
        LOG.info("Received request to update Product: {}", product.getId());
        try {
            productRepository.updateProduct(product);
            LOG.info("Product updated successfully: {}", product.getId());
            return Response.status(204).build();
        } catch (Exception e) {
            LOG.error("Error updating Product: {}", product.getId(), e);
            return Response.status(500).entity("Error updating Product").build();
        }
    }

    @DELETE
    @RolesAllowed("admin")
    @Path("/{productId}") // productID to delete
    @Operation(operationId = "deleteProduct", description = "Delete an product by ID") 
    @APIResponse(responseCode = "204", description = "Successfully deleted" )
    public Response delete(@Parameter(description = "The id of the product to delete.", required = true) @PathParam("productId") Long productId) {
        LOG.info("Received request to delete Product with ID: {}", productId);
        try {
            productRepository.deleteProduct(productId);
            LOG.info("Product deleted successfully with ID: {}", productId);
            return Response.status(204).build();
        } catch (Exception e) {
            LOG.error("Error deleting Product with ID: {}", productId, e);
            return Response.status(500).entity("Error deleting Product").build();
        }
    }

    @GET
    @Path("writefile")
    @Produces("text/plain")
    public CompletionStage<String> writeFile() {
        LOG.info("Received request to write file.");
        return productRepository.writeFile();
    }

    @GET
    @Path("readfile")
    public CompletionStage<String> readFile() {
        LOG.info("Received request to read file.");
        return productRepository.readFile()
                .thenApply(result -> {
                    LOG.info("File read successfully: " + result.length());
                    return result;
                });
    }
}