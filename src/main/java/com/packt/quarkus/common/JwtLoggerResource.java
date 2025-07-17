package com.packt.quarkus.common;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

@Path("/log-jwt")
public class JwtLoggerResource {

    private static final Logger LOG = Logger.getLogger(JwtLoggerResource.class);

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed("user") // Optional: restrict access to users with 'user' role
    public Response logJwtClaims() {
        LOG.info("🔐 Logging JWT claims for authenticated user...");

        LOG.info("Subject: " + jwt.getSubject());
        LOG.info("Name: " + jwt.getName());
        LOG.info("Issuer: " + jwt.getIssuer());
        LOG.info("Groups: " + jwt.getGroups());
        LOG.info("Raw Token: " + jwt.getRawToken());

        // Log all claims dynamically
        jwt.getClaimNames().forEach(claim -> {
            Object value = jwt.getClaim(claim);
            LOG.info("Claim [" + claim + "] = " + value);
        });

        return Response.ok("JWT claims logged successfully").build();
    }
}
