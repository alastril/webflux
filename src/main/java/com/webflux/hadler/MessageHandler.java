package com.webflux.hadler;

import com.webflux.entity.Message;
import com.webflux.repository.mysql.MessageRepository;
import com.webflux.response.StandardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
@Profile("mysql")
public class MessageHandler {
    public static final Logger LOGGER = LogManager.getLogger(MessageHandler.class);
    @Autowired
    private MessageRepository messageRepository;

    public Mono<ServerResponse> getMessageById(ServerRequest serverRequest) {
        Mono<Message> messageMono = messageRepository.findById(Long.valueOf(serverRequest.pathVariable("id")));
        return messageMono.flatMap(m ->
                ok().contentType(APPLICATION_JSON).bodyValue(m)).
                switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> getMessages(ServerRequest serverRequest) {
        return  messageRepository.
                findAll().
                collectList().
                flatMap( m ->
                        m.isEmpty() ? notFound().build() : ok().contentType(APPLICATION_JSON).bodyValue(m));

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
                switchIfEmpty(ServerResponse.badRequest().
                        bodyValue(StandardResponse.builder().responseText("id must be null or use Update point!").build()));
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
                return ServerResponse.badRequest().
                        bodyValue(StandardResponse.builder().responseText("id must be not a null").build());
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
