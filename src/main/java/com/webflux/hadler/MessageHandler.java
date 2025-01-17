package com.webflux.hadler;

import com.webflux.entity.Message;
import com.webflux.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


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

        return serverRequest.bodyToMono(Message.class).
                flatMap(message -> {
                            if(null == message.getId()) {
                                return messageRepository.save(message);
                            } else {
                                return Mono.empty();
                            }
                }
                ).
                flatMap(m -> ServerResponse.ok().bodyValue(m)).
                switchIfEmpty(ServerResponse.badRequest().build());
    }

    public Mono<ServerResponse> updateMessage(ServerRequest serverRequest) {
        Mono<Message> requestBody = serverRequest.bodyToMono(Message.class);
        return  requestBody.flatMap(message -> {
            if(null != message.getId()){
                return messageRepository.findById(message.getId()).
                        flatMap( createdMes-> {
                            messageRepository.save(message).subscribe();
                            return ServerResponse.ok().bodyValue(message);
                        });
            } else {
                return ServerResponse.badRequest().bodyValue("id == null");
            }
                     }).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteMessage(ServerRequest serverRequest) {
        final Mono<Message> message = messageRepository.findById(Long.valueOf(serverRequest.pathVariable("id")));
        return message.flatMap(m -> {
                    messageRepository.deleteById(m.getId()).subscribe();
                    return ServerResponse.ok().build();
                }).switchIfEmpty(ServerResponse.notFound().build());
    }
}
