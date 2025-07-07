package com.packt.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
class OrderDetailEndpointTest {

    @Test
    void testOrderDetailEndpoint() {

        // 1. Test there are 2 customers in the database
        given()
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body("$.size()", is(2));

        // 2. Test POST: Create a new order
        JsonObject objOrderDetail = Json.createObjectBuilder()
                .add("item", "Mac Mini M4")
                .add("price", 690)
                .build();

        Response postResponse = given()
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
        objOrderDetail = Json.createObjectBuilder()
                .add("id", createdOrderDetailId)
                .add("item", "mountain bike")
                .add("price", 100)
                .build();

        given()
                .contentType("application/json")
                .body(objOrderDetail.toString())
                .when()
                .put("/orders")
                .then()
                .statusCode(204);

        // Test GET using customerId for Updated Order
        given()
                .when().get("/orders?customerId="+createdOrderDetailId)
                .then()
                .statusCode(200)
                .body(containsString("mountain bike"));

        // 4. Test GET Single: Verify the update by fetching the order by its ID
        given()
                .when().get("/orders/" + createdOrderDetailId)
                .then()
                .statusCode(200)
                .body("item", is("mountain bike"))
                .body("price", is(100));


        // 4. Test DELETE Order #1
        given()
                .when().delete("/orders/" + createdOrderDetailId)
                .then()
                .statusCode(204);

        // Verify the Order is no longer present after deletion
        given()
                .when().get("/orders/" + createdOrderDetailId)
                .then()
                .statusCode(404); // Expecting 404 Not Found after deletion

    }
}
