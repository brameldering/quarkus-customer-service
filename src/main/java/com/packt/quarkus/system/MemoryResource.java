package com.packt.quarkus.system;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

@Path("/memory") // Base path for this resource
public class MemoryResource {

    @GET // This method handles GET requests
    @Produces(MediaType.APPLICATION_JSON) // This method will produce JSON output
    public MemoryInfoDTO getMemoryInfo() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

        long initialHeapSize = heapMemoryUsage.getInit(); // -Xms
        long maxHeapSize = heapMemoryUsage.getMax();     // -Xmx
        long usedHeapSize = heapMemoryUsage.getUsed();

        // Create an instance of our DTO and return it
        return new MemoryInfoDTO(initialHeapSize, maxHeapSize, usedHeapSize);
    }
}