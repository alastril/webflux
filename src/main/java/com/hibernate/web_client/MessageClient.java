package com.hibernate.web_client;

import com.hibernate.entity.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Profile("client")
public class MessageClient {

    private WebClient webClient;
    WebClient.Builder builder;

    @Value("${uri.client}")
    public String URI_CLIENT;

    public MessageClient(WebClient.Builder builder) {
        this.builder = builder;
    }

    @PostConstruct
    private void initConst(){
        this.webClient = builder.baseUrl(URI_CLIENT).build();
    }

    public Mono<Message> getMessage() {
        return this.webClient.get().
                uri("/message/3").
                accept(MediaType.APPLICATION_JSON).
                retrieve().
                bodyToMono(Message.class);
    }
}
