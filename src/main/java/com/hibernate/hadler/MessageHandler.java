package com.hibernate.hadler;

import com.hibernate.entity.Message;
import com.hibernate.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
@Profile("route")
public class MessageHandler {

    @Autowired
    private MessageRepository messageRepository;

    public Mono<ServerResponse> getMessageById(ServerRequest serverRequest) {
        Mono<Message> messageMono = messageRepository.findById(Long.valueOf(serverRequest.pathVariable("id")));
        return messageMono.flatMap(m ->
                ok().contentType(APPLICATION_JSON).bodyValue(m)).
                switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> getMessages(ServerRequest serverRequest) {
        final Flux<Message> message = messageRepository.findAll();

        return message.hasElements().flatMap(isAvailable -> {
                    if (isAvailable) {
                        return ok().contentType(APPLICATION_JSON).body(message, Message.class);
                    } else {
                        return notFound().build();
                    }
                }

        );

        /* 2nd variant:
                boolean check;
                try {
                    check = message.hasElements().toFuture().get();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return check ? ok().contentType(APPLICATION_JSON).body(message, Message.class)
                        : notFound().build();
         */
    }

    public Mono<ServerResponse> saveMessage(ServerRequest serverRequest) {
        final Mono<Message> message = serverRequest.bodyToMono(Message.class);
        message.flatMap( messageRepository::save).subscribe();
        return  ok().contentType(APPLICATION_JSON).build();
    }
}
