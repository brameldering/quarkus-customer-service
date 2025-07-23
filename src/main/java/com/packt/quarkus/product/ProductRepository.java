package com.packt.quarkus.product;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonArray;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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


    public CompletionStage<String> writeFile( ) {

        LOG.info("Start writeFile.");
        try {
            Path tmpDir = Paths.get("./tmp");
            if (!Files.exists(tmpDir)) {
                Files.createDirectories(tmpDir);
            }
        } catch (Exception e) {
             LOG.error("Error creating directory", e);
        }

        JsonArrayBuilder jsonArray = jakarta.json.Json.createArrayBuilder();
        for (Product product:products) {
            jsonArray.add(jakarta.json.Json.createObjectBuilder().
                    add("id", product.getId())
                    .add("name", product.getName())
                    .add("price", product.getPrice())
                    .build());
        }
        JsonArray array = jsonArray.build();
        LOG.info("Writing product file JsonArray: "+ array);
        CompletableFuture<String> future = new CompletableFuture<>();
        vertx.fileSystem().writeFile(filePath, Buffer.buffer(array.toString()), handler -> {
            LOG.info("In handler.");
            LOG.info("Resolved path: " + Paths.get(filePath).toAbsolutePath());
            if (handler.succeeded()) {
                LOG.info("Successfully written JSON file in " +filePath);
                future.complete("Written JSON file in " +filePath);
            } else {
                System.err.println("Error while writing in file: " + handler.cause().getMessage());
            }
        });
        LOG.info("Just before return future");
        return future;
    }


    public CompletionStage<String> readFile() {
        LOG.info("Start readFile.");
        // When complete, return the content to the client
        CompletableFuture<String> future = new CompletableFuture<>();

        long start = System.nanoTime();

        // Delay reply by 100ms
        vertx.setTimer(100, l -> {
            // Compute elapsed time in milliseconds
            long duration = MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS);

            vertx.fileSystem().readFile(filePath, ar -> {
                if (ar.succeeded()) {
                    String response = ar.result().toString("UTF-8");
                    String first160chars = response.length() > 160 ? response.substring(0, 157)+"..." : response;
                    LOG.info("Successfully read file: " + first160chars);
                    future.complete(response);
                } else {
                    future.complete("Cannot read the file: " + ar.cause().getMessage());
                }
            });
        });
        return future;
    }
}
