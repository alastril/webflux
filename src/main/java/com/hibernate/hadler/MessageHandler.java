package com.hibernate.hadler;

import com.hibernate.entity.Message;
import com.hibernate.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Profile("route")
public class MessageHandler {

    @Autowired
    private MessageRepository messageRepository;

    public Mono<ServerResponse> getMessage(ServerRequest serverRequest) {
        Optional<Message> message =
                messageRepository.findById(Long.valueOf(serverRequest.pathVariable("id")));
        Mono<Message> messageMono = message.map(Mono::just).orElseGet(Mono::empty);
        return messageMono.flatMap(m ->
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(m)).
                switchIfEmpty(ServerResponse.notFound().build());
    }
}
