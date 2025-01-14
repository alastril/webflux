package com.hibernate.controllers;

import com.hibernate.web_client.MessageClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.netty.http.server.HttpServerConfig;
import reactor.netty.transport.TransportConfig;


@Component
@Profile("client")
public class WebFluxController {

    @Autowired
    MessageClient client;

    @PostConstruct
    public void init() {
        client.getMessage().map(m -> {
//            HttpServerConfig
//            TransportConfig
                System.out.println(">> message = " + m); return m;}).subscribe();
    }
}
