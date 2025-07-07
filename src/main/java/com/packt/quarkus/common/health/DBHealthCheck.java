package com.packt.quarkus.common.health;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.Socket;

// You typically want to specify if it's a @Liveness, @Readiness, or @Startup check.
// @Health is a generic annotation, but for specific endpoints, it's better to be precise.
// For a database connection, @Readiness is usually the most appropriate.
@Readiness
@ApplicationScoped
public class DBHealthCheck implements HealthCheck {

    @ConfigProperty(name = "db.host")
    String host;

    @ConfigProperty(name = "db.port")
    Integer port;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Database connection health check");
        try {
            serverListening(host,port);
            responseBuilder.up();
        } catch (Exception e) {
            // cannot access the database
            responseBuilder.down()
                    .withData("error", e.getMessage());
        }

        return responseBuilder.build();
    }

    private void serverListening(String host, int port) throws IOException {
        Socket s = new Socket(host, port);
        s.close();
    }
}