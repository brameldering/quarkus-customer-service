package com.packt.quarkus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.StringReader;

import static io.restassured.RestAssured.given;

@ApplicationScoped
public class GetTokenFromKeyCloak {

    @ConfigProperty(name = "keycloak.url")
    String keyCloakURL;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String keyCloakClientSecret;

     public String getToken(String username, String password) {

         RestAssured.baseURI = keyCloakURL;

         // Get Test user token
         Response response = given().urlEncodingEnabled(true)
                 .auth().preemptive().basic("quarkus-client", keyCloakClientSecret)
                 .param("grant_type", "password")
                 .param("client_id", "quarkus-client")
                 .param("username", username)
                 .param("password", password)
                 .header("Accept", ContentType.JSON.getAcceptHeader())
                 .post("/realms/quarkus-realm/protocol/openid-connect/token")
                 .then().statusCode(200).extract()
                 .response();
         JsonReader jsonReader = Json.createReader(new StringReader(response.getBody().asString()));
         JsonObject object = jsonReader.readObject();
         return object.getString("access_token");
     }
}
