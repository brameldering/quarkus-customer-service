package com.packt.quarkus.scheduler;

import com.packt.quarkus.customer.CustomerEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public class TokenGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TokenGenerator.class);

    private String token;

    public String getToken() {
        return token;
    }

    @Scheduled(every="30s")
    void generateToken() {
        token= UUID.randomUUID().toString();
        LOG.info("Generated token: {}", token);
    }
}
