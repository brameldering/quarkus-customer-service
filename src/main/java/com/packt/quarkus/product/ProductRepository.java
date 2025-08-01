package com.packt.quarkus.product;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonArray;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@ApplicationScoped
public class ProductRepository {
    @Inject
    Vertx vertx;

    @ConfigProperty(name="file.path")
    String filePath;

    List<Product> products = new ArrayList<>();
    int counter;

    private static final Logger LOG = LoggerFactory.getLogger(ProductRepository.class);

    public long getNextProductId() {
        return counter++;
    }

    public List<Product> findAll() {
        return products;
    }

    public Product findProductById(Long id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }

    //    @Transactional
    public Product createProduct(Product product) {
        product.setId(getNextProductId());
        findAll().add(product);
        return product;
    }

    //    @Transactional
    public void updateProduct(Product product) {
        Product productToUpdate = findProductById(product.getId());
        productToUpdate.setName(product.getName());
        productToUpdate.setPrice(product.getPrice());
    }

//    @Transactional
    public void deleteProduct(Long orderId) {
        Product product = findProductById(orderId);
        findAll().remove(product);
    }

    public CompletionStage<String> writeFile() {

        LOG.info("Start writeFile.");

        // The Vert.x FileSystem API is used for non-blocking I/O operations.
        // We chain the mkdir and writeFile operations using compose().
        return vertx.fileSystem().mkdirs("./tmp")
                .compose(v -> {
                    // This part runs only if the directory was created successfully.
                    // Build the JSON array to be written to the file.
                    JsonArrayBuilder jsonArray = jakarta.json.Json.createArrayBuilder();
                    for (Product product : products) {
                        jsonArray.add(jakarta.json.Json.createObjectBuilder()
                                .add("id", product.getId())
                                .add("name", product.getName())
                                .add("price", product.getPrice())
                                .build());
                    }
                    JsonArray array = jsonArray.build();
                    LOG.info("Writing product file JsonArray: " + array);

                    // Return a new Future for the writeFile operation.
                    return vertx.fileSystem().writeFile(filePath, Buffer.buffer(array.toString()));
                })
                .map(v -> {
                    // This is the success path, executed after the file is written.
                    LOG.info("Successfully written JSON file in " + filePath);
                    LOG.info("Resolved path: " + Paths.get(filePath).toAbsolutePath());
                    return "Written JSON file in " + filePath;
                })
                .otherwise(throwable -> {
                    // This is the failure path. The original exception is passed to us.
                    // We log the error and return a failed Future,
                    // which will be propagated up the chain.
                    LOG.error("Error while writing to file: " + throwable.getMessage());
                    return Future.failedFuture(new RuntimeException("Failed to write file: " + throwable.getMessage(), throwable)).toString();
                })
                .toCompletionStage(); // Convert the Vert.x Future to a CompletionStage.
    }

    public CompletionStage<String> readFile() {

        LOG.info("Start readFile.");

        // Create a Vert.x Promise to handle the asynchronous result.
        Promise<String> promise = Promise.promise();

        long start = System.nanoTime();

        // Use Vert.x's non-blocking file system API to read the file.
        vertx.fileSystem().readFile(filePath, ar -> {
            // This handler is executed when the asynchronous operation completes.
            long duration = MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS);

            if (ar.succeeded()) {
                String response = ar.result().toString("UTF-8");
                String first160chars = response.length() > 160 ? response.substring(0, 157) + "..." : response;
                LOG.info("Successfully read file: " + first160chars + " (took " + duration + "ms)");
                promise.complete(response);
            } else {
                // On failure, log the error and fail the promise with the cause.
                // This propagates the original exception for proper error handling.
                LOG.error("Error reading file: " + ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        // Convert the Vert.x Future to a CompletionStage for the return type.
        return promise.future().toCompletionStage();
    }

}
