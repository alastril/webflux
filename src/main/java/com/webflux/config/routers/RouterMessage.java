package com.webflux.config.routers;

import com.webflux.hadler.FileHandler;
import com.webflux.hadler.MessageHandler;
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

    @Autowired
    FileHandler fileHandler;

    @Bean
    public RouterFunction<ServerResponse> routerMess() {
      return RouterFunctions.route(
                        GET("/message/{id}").and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::getMessageById).
                        andRoute(GET("/message").and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::getMessages).
                        andRoute(POST("/message").and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::saveMessage).
                        andRoute(PUT("/message").and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::updateMessage).
                        andRoute(DELETE("/message/{id}").and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::deleteMessage).
                        andRoute(POST("/file").and(accept(MediaType.MULTIPART_FORM_DATA)),
                                fileHandler::saveMultiPartFile).
                        andRoute(GET("/file/{id_file}/{file_name}"),
                                fileHandler::getFile);
    }

}
