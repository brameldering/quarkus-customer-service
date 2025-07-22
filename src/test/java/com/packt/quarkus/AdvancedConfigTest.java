package com.packt.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdvancedConfigTest {

    @BeforeAll
    void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:8444";
    }

    @Test
    public void testHelloEndpoint() {

        // You need to copy the file customconfig.properties to the /tmp folder
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("custom greeting"));

        given()
                .when().get("/year")
                .then()
                .body(is("2025"));

        given()
                .pathParam("id", 1)
                .when().get("/pets/{id}")
                .then()
                .body(is("cat"));

        given()
                .when().get("/students")
                .then()
                .body(is("Lucy"));

        given()
                .when().get("/email")
                .then()
                .body(is("johnsmith@gmail.com"));

        given()
                .when().get("/isUser")
                .then()
                .body(is("true"));

    }

}

