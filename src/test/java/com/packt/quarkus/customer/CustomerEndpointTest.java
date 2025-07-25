package com.packt.quarkus.customer;

import com.packt.quarkus.keycloaktestresource.GetTokenFromKeyCloak;
import com.packt.quarkus.keycloaktestresource.KeycloakTestResource;
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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;

import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@QuarkusTest
@QuarkusTestResource(KeycloakTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerEndpointTest {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerEndpointTest.class);

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

        // 1. Test POST: Create a new customer
        JsonObject newCustomerJson = Json.createObjectBuilder()
                .add("firstName", "John")
                .add("lastName", "Smith")
                .build();

        // following no longer needed because of @BeforeAll
//        RestAssured.baseURI = "https://localhost:8444"; // instead of http://localhost:8081

        LOG.info("Test create customer.");
        Response postResponse = given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .contentType("application/json")
                .body(newCustomerJson.toString())
                .when()
                .post("/customers")
                .then()
                .statusCode(201) // Expecting 201 Created for a successful POST
                .extract()
                .response();

        // Extract the generated ID from the response
        Long createdCustomerId = postResponse.jsonPath().getLong("id");

        // Assert that the ID is valid (e.g., greater than 0, as sequences usually start from 1)
        postResponse.then().body("id", greaterThan(0));

        // 2. Test GET All: Verify the newly created customer is in the list
        // This check is a bit broad, but combined with size() and containsInAnyOrder later, it's fine.
        LOG.info("Test get all customers.");
        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body(containsString("John"),
                        containsString("Smith"));

        // 3. Test PUT: Update the newly created customer
        JsonObject updatedCustomerJson = Json.createObjectBuilder()
                .add("id", createdCustomerId) // Use the dynamically created ID
                .add("firstName", "Donald")
                .add("lastName", "Duck")
                .build();

        LOG.info("Test update customer.");
        given()
                .auth()
                .oauth2(adminToken)
                .contentType("application/json")
                .body(updatedCustomerJson.toString())
                .when()
                .put("/customers")
                .then()
                .statusCode(204); // Expecting 204 No Content for a successful PUT (update)

        // 4. Test GET Single: Verify the update by fetching the customer by its ID
        LOG.info("Test get customer by id.");
        given()
                .auth()
                .oauth2(userToken)
                .when().get("/customers/" + createdCustomerId)
                .then()
                .statusCode(200)
                .body("firstName", is("Donald"))
                .body("lastName", is("Duck"));

        // 5. Test GET All after Update: Verify all customers, including the updated one and those from import.sql
        LOG.info("Test get all customers.");
        given()
                .auth()
                .oauth2(adminToken)
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body("size()", is(3)) // Assuming 2 customers from import.sql + 1 created in test
                .body("firstName", containsInAnyOrder("John", "Fred", "Donald")); // Check names from all customers

        // 6. Test DELETE: Delete the customer created in this test
        LOG.info("Test delete customer.");
        given()
                .auth()
                .oauth2(adminToken)
                .contentType("application/json")
                .when()
                .delete("/customers/" + createdCustomerId) // Use the dynamically created ID and path parameter
                .then()
                .statusCode(204); // Expecting 204 No Content for a successful DELETE

        // Verify the customer is no longer present after deletion
        LOG.info("Test customer is no longer present after delete.");
        given()
                .auth()
                .oauth2(adminToken)
                .when().get("/customers/" + createdCustomerId)
                .then()
                .statusCode(404); // Expecting 404 Not Found after deletion

        // Verify total count after deletion
        LOG.info("Test count of customers.");
        given()
                .auth()
                .oauth2(adminToken)
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body("size()", is(2)); // Should be back to 2 customers from import.sql
    }
}
