package com.packt.quarkus.product;

import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProductRepository {
//    @Inject
//    Vertx vertx;
//
//    @ConfigProperty(name="file.path")
//    String filePath;

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
}
