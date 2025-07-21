package com.packt.quarkus;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

@QuarkusTest
@QuarkusTestResource(KeycloakTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderDetailEndpointTest {

    private static final Logger LOG = LoggerFactory.getLogger(OrderDetailEndpointTest.class);

    @Inject
    GetTokenFromKeyCloak getTokenFromKeyCloak;

    @BeforeAll
    void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:8444";
    }

    @Test
    void testOrderDetailEndpoint() {

        // Get Test user token
        LOG.info("Get Test user token.");
        String userToken = getTokenFromKeyCloak.getToken("test", "test");

        // Get Admin user token
        LOG.info("Get Admin user token.");
        String adminToken = getTokenFromKeyCloak.getToken("admin", "test");

//        RestAssured.baseURI = "https://localhost:8444"; // instead of http://localhost:8081

        // 1. Test there are 2 customers in the database
        LOG.info("Test there are 2 customers in the database.");
        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body("$.size()", is(2));

        // 2. Test POST: Create a new order
        LOG.info("Test POST: Create a new order.");
        JsonObject objOrderDetail = Json.createObjectBuilder()
                .add("item", "Mac Mini M4")
                .add("price", 690)
                .build();

        Response postResponse = given()
                .auth()
                .oauth2(adminToken)
                .contentType("application/json")
                .body(objOrderDetail.toString())
                .when()
                .post("/orders/1")
                .then()
                .statusCode(201) // Expecting 201 Created for a successful POST
                .extract()
                .response();

        // Extract the generated ID from the response
        Long createdOrderDetailId = postResponse.jsonPath().getLong("id");

        // Assert that the ID is valid (e.g., greater than 0, as sequences usually start from 1)
        postResponse.then().body("id", greaterThan(0));

        // 3. Test UPDATE Order #1
        LOG.info("Test update order.");
        objOrderDetail = Json.createObjectBuilder()
                .add("id", createdOrderDetailId)
                .add("item", "mountain bike")
                .add("price", 100)
                .build();

        given()
                .auth()
                .oauth2(adminToken)
                .contentType("application/json")
                .body(objOrderDetail.toString())
                .when()
                .put("/orders")
                .then()
                .statusCode(204);

        // Test with wrong password (unauthorized)
        LOG.info("Test with wrong password (unauthorized).");
        given()
                .auth()
                .preemptive()
                .basic("admin", "bram")
                .contentType("application/json")
                .body(objOrderDetail.toString())
                .when()
                .put("/orders")
                .then()
                .statusCode(anyOf(is(401), is(403)));

        // Test with wrong username (forbidden)
        LOG.info("Test with wrong username (forbidden).");
        given()
                .auth()
                .preemptive()
                .basic("bram", "bram")
                .contentType("application/json")
                .body(objOrderDetail.toString())
                .when()
                .put("/orders")
                .then()
                .statusCode(anyOf(is(401), is(403)));

        // Test GET using customerId for Updated Order
        LOG.info("Test GET using customerId for Updated Order.");
        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .when().get("/orders?customerId="+createdOrderDetailId)
                .then()
                .statusCode(200)
                .body(containsString("mountain bike"));

        // 4. Test GET Single: Verify the update by fetching the order by its ID
        LOG.info("Test GET Single: Verify the update by fetching the order by its ID.");
        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .when().get("/orders/" + createdOrderDetailId)
                .then()
                .statusCode(200)
                .body("item", is("mountain bike"))
                .body("price", is(100));


        // 4. Test DELETE Order #1
        LOG.info("Test DELETE Order.");
        given()
                .auth()
                .oauth2(adminToken)
                .when().delete("/orders/" + createdOrderDetailId)
                .then()
                .statusCode(204);

        // Verify the Order is no longer present after deletion
        LOG.info("Verify the Order is no longer present after deletion.");
        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .when().get("/orders/" + createdOrderDetailId)
                .then()
                .statusCode(404); // Expecting 404 Not Found after deletion
    }
}
