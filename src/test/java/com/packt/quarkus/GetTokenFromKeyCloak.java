package com.packt.quarkus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

import static io.restassured.RestAssured.given;

@ApplicationScoped
public class GetTokenFromKeyCloak {

    private static final Logger LOG = LoggerFactory.getLogger(GetTokenFromKeyCloak.class);

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String fullKeyCloakURL;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String keyCloakClientSecret;

     public String getToken(String username, String password) {

         LOG.info("=====>>> fullKeyCloakURL: "+ fullKeyCloakURL);
         LOG.info("=====>>> clientId: "+ clientId);
         LOG.info("=====>>> keyCloakClientSecret: "+ keyCloakClientSecret);

         RestAssured.baseURI = fullKeyCloakURL;

         // Get Test user token
         Response response = given().urlEncodingEnabled(true)
                 .auth().preemptive().basic(clientId, keyCloakClientSecret)
                 .param("grant_type", "password")
                 .param("client_id", clientId)
                 .param("username", username)
                 .param("password", password)
                 .header("Accept", ContentType.JSON.getAcceptHeader())
//                 .post("/realms/quarkus-realm/protocol/openid-connect/token")
                 .post("/protocol/openid-connect/token")
                 .then().statusCode(200).extract()
                 .response();
         JsonReader jsonReader = Json.createReader(new StringReader(response.getBody().asString()));
         JsonObject object = jsonReader.readObject();
         return object.getString("access_token");
     }
}
