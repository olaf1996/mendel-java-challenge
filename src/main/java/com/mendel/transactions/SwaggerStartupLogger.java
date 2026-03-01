package com.mendel.transactions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SwaggerStartupLogger {

    @Value("${server.port:8080}")
    private String port;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String base = "http://localhost:" + port;
        System.out.println();
        System.out.println("--- Swagger UI ---  " + base + "/swagger-ui/index.html");
        System.out.println("--- API (OpenAPI) - " + base + "/v3/api-docs");
        System.out.println();
    }
}
