package com.packt.quarkus.common.health;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Paths;

@Readiness
@ApplicationScoped
public class ReadinessHealthCheck implements HealthCheck {

    @ConfigProperty(name = "health.readiness.lock-file-path", defaultValue = "/tmp/tmp.lck")
    String lockFilePath;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("File system Readiness check");

        boolean lockFileExists = Files.exists(Paths.get(lockFilePath));
        if (!lockFileExists) {
            responseBuilder.up();
        }
        else {
            responseBuilder.down()
                    .withData("file", lockFilePath) // Add the actual file path to data
                    .withData("error", "Lock file detected! Application not ready.");
        }
        return responseBuilder.build();
    }
}
