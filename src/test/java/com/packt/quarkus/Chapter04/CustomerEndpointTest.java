package com.packt.quarkus.Chapter04;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class CustomerEndpointTest {
    @Test
    void testCustomerEndpoint() {
        System.out.println("Start of testCustomerEndpoint");

        JsonObject obj = Json.createObjectBuilder()
                .add("firstName", "John")
                .add("lastName", "Smith").build();

        // Test POST
        given()
                .contentType("application/json")
                .body(obj.toString())
                .when()
                .post("/customers")
                .then()
                .statusCode(201);

        // Test GET
        given()
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body(containsString("John"),
                        containsString("Smith"));

        obj = Json.createObjectBuilder()
                .add("id", "0")
                .add("firstName", "Donald")
                .add("lastName", "Duck").build();

        // Test PUT
        given()
                .contentType("application/json")
                .body(obj.toString())
                .when()
                .put("/customers")
                .then()
                .statusCode(204);

        // Test GET after UPDATE
        given()
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body(containsString("Donald"),
                        containsString("Duck"));

        // Test DELETE
        given()
                .contentType("application/json")
                .when()
                .delete("/customers?id=0")
                .then()
                .statusCode(204);

        System.out.println("End of testCustomerEndpoint");
    }
}