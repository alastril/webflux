package com.webflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webflux.config.TestConfig;
import com.webflux.entity.Message;
import com.webflux.hadler.FileHandler;
import com.webflux.repository.mysql.MessageRepository;
import com.webflux.response.StandardResponse;
import com.webflux.util.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@ActiveProfiles(profiles = {"mysql", "test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient //for webFlux web-client
@TestInstance(TestInstance.Lifecycle.PER_CLASS)// need for @BeforeAll - annotation if no need static
@Import(TestConfig.class)
public class TestMessageWebFlux {
    @MockitoBean
    private MessageRepository messageRepository;

    @MockitoBean
    private Utils utils;

    @MockitoBean
    private FileHandler fileHandler;

    @Autowired
    private WebTestClient webClient;

    private List<Message> messages;

    @BeforeAll
    public void init() {
        messages = Arrays.asList(new Message(1L, "test_message"), new Message(2L, "test"));
    }

    @Test
    public void testGetMessage() {

        Message message = messages.get(1);
        Mockito.when(messageRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Mono.just(message));
        webClient.get().
                uri("/message/{id}", 1L)
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Message.class).isEqualTo(message);
        Mockito.verify(messageRepository, Mockito.times(1)).
                findById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testGetMessageNoResult() {

        Mockito.when(messageRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Mono.empty());
        webClient.get().
                uri("/message/{id}", 1L)
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isNotFound(),
                        responseSpec -> responseSpec.expectBody().isEmpty()
                );
        Mockito.verify(messageRepository, Mockito.times(1)).
                findById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testGetAllMessageNoResult() {

        Mockito.when(messageRepository.findAll()).thenReturn(Flux.empty());
        webClient.get().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isNotFound(),
                        responseSpec -> responseSpec.expectBody().isEmpty()
                );
        Mockito.verify(messageRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void testGetAllMessages() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(messageRepository.findAll()).thenReturn(Flux.fromIterable(messages));
        webClient.get().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody().json(objectMapper.writeValueAsString(messages));
        Mockito.verify(messageRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void testPostMessage() {
        Mockito.when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .thenReturn(Mono.just(messages.get(0)));
        Message requestMess =
                Message.builder().text(messages.get(0).getText()).build();
        webClient.post().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(requestMess)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody(Message.class).isEqualTo(messages.get(0));
        Mockito.verify(messageRepository, Mockito.times(1)).
                save(ArgumentMatchers.any(Message.class));
    }

    @Test
    public void testPostMessageBadRequest() {
        Mockito.when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .thenReturn(Mono.just(messages.get(0)));
        webClient.post().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(messages.get(0))
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isBadRequest()
                ).expectBody(StandardResponse.class).isEqualTo(
                                StandardResponse.builder().
                                        responseText("id must be null or use Update point!").build());
        Mockito.verify(messageRepository, Mockito.times(0)).
                save(ArgumentMatchers.any(Message.class));
    }

    @Test
    public void testPutMessage() {
        ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(messageRepository.findById(ArgumentMatchers.any(Long.class)))
                .thenReturn(Mono.just(messages.get(0)));
        Mockito.when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .thenReturn(Mono.just(messages.get(0)));
        Message requestMess =
                Message.builder().
                        id(messages.get(0).getId()).
                        text(messages.get(0).getText().concat("test!")).
                        build();
        webClient.put().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(requestMess)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody(Message.class).isEqualTo(requestMess);
        Mockito.verify(messageRepository, Mockito.times(1)).
                save(ArgumentMatchers.any(Message.class));
        Mockito.verify(messageRepository, Mockito.times(1)).
                findById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testPutWithIdNullMessageBadRequest() {
        Message requestMess =
                Message.builder().
                        id(null).
                        text(messages.get(0).getText().concat("test!")).
                        build();
        webClient.put().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(requestMess)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isBadRequest()
                ).expectBody(StandardResponse.class).
                isEqualTo(StandardResponse.builder().responseText("id must be not a null").build());
        Mockito.verify(messageRepository, Mockito.times(0)).
                save(ArgumentMatchers.any(Message.class));
        Mockito.verify(messageRepository, Mockito.times(0)).
                findById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testPutNotFoundMessage() {
        Mockito.when(messageRepository.findById(ArgumentMatchers.any(Long.class)))
                .thenReturn(Mono.empty());
        Message requestMess =
                Message.builder().
                        id(Long.MAX_VALUE).
                        text(messages.get(0).getText().concat("test!")).
                        build();
        webClient.put().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(requestMess)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isNotFound()
                ).expectBody().isEmpty();
        Mockito.verify(messageRepository, Mockito.times(0)).
                save(ArgumentMatchers.any(Message.class));
        Mockito.verify(messageRepository, Mockito.times(1)).
                findById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testDeleteWithIdMessage() {
        Mockito.when(messageRepository.findById(ArgumentMatchers.any(Long.class)))
                .thenReturn(Mono.just(messages.get(0)));
        Mockito.when(messageRepository.deleteById(ArgumentMatchers.any(Long.class)))
                .thenReturn(Mono.empty());
        webClient.delete().
                uri("/message/{id}", messages.get(0).getId())
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody().isEmpty();
        Mockito.verify(messageRepository, Mockito.times(1)).
                findById(ArgumentMatchers.any(Long.class));
        Mockito.verify(messageRepository, Mockito.times(1)).
                deleteById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testDeleteWithIdNotFoundMessage() {
        Mockito.when(messageRepository.findById(ArgumentMatchers.any(Long.class)))
                .thenReturn(Mono.empty());
        webClient.delete().
                uri("/message/{id}", Long.MAX_VALUE)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isNotFound()
                ).expectBody().isEmpty();
        Mockito.verify(messageRepository, Mockito.times(1)).
                findById(ArgumentMatchers.any(Long.class));
        Mockito.verify(messageRepository, Mockito.times(0)).
                deleteById(ArgumentMatchers.any(Long.class));
    }
}
