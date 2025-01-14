package com.hibernate.config.routers;

import com.hibernate.hadler.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@Profile("route")
public class RouterMessage {

    @Autowired
    MessageHandler messageHandler;

    @Bean
    public RouterFunction<ServerResponse> routerMess() {
        return RouterFunctions.route(GET("/message/{id}").
                                and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::getMessageById).
                        andRoute(GET("/message").and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::getMessages).
                        andRoute(POST("/message").and(accept(MediaType.APPLICATION_JSON)),
                        messageHandler::saveMessage);
    }

}
