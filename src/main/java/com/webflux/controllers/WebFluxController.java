package com.webflux.controllers;

import com.webflux.web_client.MessageClient;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Profile("client")
public class WebFluxController {
    public static final Logger LOGGER = LogManager.getLogger(WebFluxController.class);

    @Autowired
    MessageClient client;

    @PostConstruct
    public void init() {
        client.setMessage().map(m -> {
            LOGGER.info(">> message11 = {}", m); return m;}).subscribe();
        client.getMessage().map(m -> {
            LOGGER.info(">> message = {}", m); return m;}).subscribe();

    }
}
