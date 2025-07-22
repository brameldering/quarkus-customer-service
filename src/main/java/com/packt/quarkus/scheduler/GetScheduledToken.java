package com.packt.quarkus.scheduler;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/token")
public class GetScheduledToken {
    @Inject
    TokenGenerator tokenGenerator;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getToken() {
        return tokenGenerator.getToken();
    }

}
