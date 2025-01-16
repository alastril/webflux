package com.webflux.controllers;

import com.webflux.web_client.MessageClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Profile("client")
public class WebFluxController {

    @Autowired
    MessageClient client;

    @PostConstruct
    public void init() {
        client.setMessage().map(m -> {
            System.out.println(">> message11 = " + m); return m;}).subscribe();
        client.getMessage().map(m -> {
            System.out.println(">> message = " + m); return m;}).subscribe();

    }
}
