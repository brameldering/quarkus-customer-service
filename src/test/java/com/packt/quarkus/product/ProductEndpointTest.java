package com.packt.quarkus.product;

import com.packt.quarkus.keycloaktestresource.GetTokenFromKeyCloak;
import com.packt.quarkus.keycloaktestresource.KeycloakTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;


@QuarkusTest
@QuarkusTestResource(KeycloakTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductEndpointTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProductEndpointTest.class);

    @Inject
    GetTokenFromKeyCloak getTokenFromKeyCloak;

    @BeforeAll
    void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:8444";
    }

    @Test
    void testCustomerEndpoint() {

        // Get Test user token
        LOG.info("Get Test user token.");
        String userToken = getTokenFromKeyCloak.getToken("test", "test");

        // Get Admin user token
        LOG.info("Get Admin user token.");
        String adminToken = getTokenFromKeyCloak.getToken("admin", "test");

        // 1. Test POST: Create a new product
        JsonObject newProductJson = Json.createObjectBuilder()
                .add("name", "Apple iMac 2013")
                .add("price", 1690)
                .build();

        // following no longer needed because of @BeforeAll
//        RestAssured.baseURI = "https://localhost:8444"; // instead of http://localhost:8081

        LOG.info("Test create product.");
        Response postResponse = given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .contentType("application/json")
                .body(newProductJson.toString())
                .when()
                .post("/products")
                .then()
                .statusCode(201) // Expecting 201 Created for a successful POST
                .extract()
                .response();

        // Extract the generated ID from the response
        Long createdProductId = postResponse.jsonPath().getLong("id");

        LOG.info("Created product with ID: {}", createdProductId);

        // 2. Test GET All: Verify the newly created product is in the list
        // This check is a bit broad, but combined with size() and containsInAnyOrder later, it's fine.
        LOG.info("Test get all products.");
        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .when().get("/products")
                .then()
                .statusCode(200)
                .body(containsString("Apple iMac 2013"));

        // 3. Test PUT: Update the newly created product
        JsonObject updatedProductJson = Json.createObjectBuilder()
                .add("id", createdProductId) // Use the dynamically created ID
                .add("name", "Mac Mini M4")
                .add("price", 390)
                .build();

        LOG.info("Test update product.");
        given()
                .auth()
                .oauth2(adminToken)
                .contentType("application/json")
                .body(updatedProductJson.toString())
                .when()
                .put("/products")
                .then()
                .statusCode(204); // Expecting 204 No Content for a successful PUT (update)

        // 4. Test GET Single: Verify the update by fetching the product by its ID
        LOG.info("Test get product by id.");
        given()
                .auth()
                .oauth2(userToken)
                .when().get("/products/" + createdProductId)
                .then()
                .statusCode(200)
                .body(containsString("Mac Mini M4"));

        // 5. Test GET All after Update: Verify all products, including the updated one and those from import.sql
        LOG.info("Test get all products.");
        given()
                .auth()
                .oauth2(adminToken)
                .when().get("/products")
                .then()
                .statusCode(200)
                .body("size()", is(1)) // Assuming 2 products from import.sql + 1 created in test
                .body(containsString("Mac Mini M4"));

        // 6. Test DELETE: Delete the product created in this test
        LOG.info("Test delete product.");
        given()
                .auth()
                .oauth2(adminToken)
                .contentType("application/json")
                .when()
                .delete("/products/" + createdProductId) // Use the dynamically created ID and path parameter
                .then()
                .statusCode(204); // Expecting 204 No Content for a successful DELETE

        // Verify the product is no longer present after deletion
        LOG.info("Test product is no longer present after delete.");
        given()
                .auth()
                .oauth2(adminToken)
                .when().get("/products/" + createdProductId)
                .then()
                .statusCode(404); // Expecting 404 Not Found after deletion

        // Verify total count after deletion
        LOG.info("Test count of products.");
        given()
                .auth()
                .oauth2(adminToken)
                .when().get("/products")
                .then()
                .statusCode(200)
                .body("size()", is(0)); // Should be back to 2 products from import.sql
    }
}
