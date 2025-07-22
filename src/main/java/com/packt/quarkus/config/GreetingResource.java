package com.packt.quarkus.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class GreetingResource {
    @ConfigProperty(name = "year",defaultValue = "2025")
    Integer year;

    @ConfigProperty(name = "greeting" )
    String greeting;

    @ConfigProperty(name = "isUser", defaultValue = "false")
    Boolean isUser;

    @ConfigProperty(name = "students")
    List<String> studentList;

    @ConfigProperty(name = "pets")
    String[] petsArray;
    @Inject
    Config config;

    @ConfigProperty(name = "customconfig")
    CustomConfigValue value;

    @Path("/hello")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return greeting;
    }

    @Path("/year")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Integer getYear() {
        return year;
    }

    @Path("/pets/{id}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPets(@PathParam("id") Integer id) {
        return petsArray[id];
    }

    @Path("/students")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getLastStudent() {
        return studentList.get(studentList.size() - 1);
    }

    @Path("/email")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getEmail() {
        return value.getEmail();
    }

    @Path("/isUser")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean isUser() {
        return isUser;
    }

}

