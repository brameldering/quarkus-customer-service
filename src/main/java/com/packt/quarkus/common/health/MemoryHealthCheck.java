package com.packt.quarkus.common.health;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class MemoryHealthCheck implements HealthCheck {
    @ConfigProperty(name = "health.memory.free-threshold-bytes", defaultValue = "1024000000")
    long thresholdMin;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("MemoryHealthCheck Liveness check");
        long freeMemory = Runtime.getRuntime().freeMemory();

        if (freeMemory >= thresholdMin) {
            responseBuilder.up()
                    .withData("freeMemoryBytes", freeMemory)
                    .withData("requiredMemoryBytes", thresholdMin);
        }
        else {
            responseBuilder.down()
                    .withData("freeMemoryBytes", freeMemory) // Actual free memory
                    .withData("requiredMemoryBytes", thresholdMin) // Configured minimum memory threshold
                    .withData("error", "Not enough free memory! Please restart application");
        }
        return responseBuilder.build();
    }

}
