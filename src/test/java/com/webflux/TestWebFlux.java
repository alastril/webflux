package com.webflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webflux.entity.File;
import com.webflux.entity.Message;
import com.webflux.repository.FileRepository;
import com.webflux.repository.MessageRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;


@ActiveProfiles(profiles = {"route", "test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)// need for BeforeAll annotation if no need static
public class TestWebFlux {
    private static final String FILE_NAME_IMG_JPG = "test_img.jpg";
    private static final String FILE_NAME_IMG_SMALL_JPG = "test_jpg_small.jpg";
    @MockitoBean
    private MessageRepository messageRepository;

    @MockitoBean
    private FileRepository fileRepository;

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
        Mockito.verify(messageRepository, Mockito.times(1)).findById(ArgumentMatchers.any(Long.class));
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
        Mockito.verify(messageRepository, Mockito.times(1)).findById(ArgumentMatchers.any(Long.class));
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
    public void testPostMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .thenReturn(Mono.just(messages.get(0)));
        Message requestMess =
                Message.builder().message(messages.get(0).getMessage()).build();
        webClient.post().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(requestMess)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody().json(objectMapper.writeValueAsString(messages.get(0)));
        Mockito.verify(messageRepository, Mockito.times(1)).save(ArgumentMatchers.any(Message.class));
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
                ).expectBody().isEmpty();
        Mockito.verify(messageRepository, Mockito.times(0)).save(ArgumentMatchers.any(Message.class));
    }

    @Test
    public void testPutMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(messageRepository.findById(ArgumentMatchers.any(Long.class)))
                .thenReturn(Mono.just(messages.get(0)));
        Mockito.when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .thenReturn(Mono.just(messages.get(0)));
        Message requestMess =
                Message.builder().
                        id(messages.get(0).getId()).
                        message(messages.get(0).getMessage().concat("test!")).
                        build();
        webClient.put().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(requestMess)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody().json(objectMapper.writeValueAsString(requestMess));
        Mockito.verify(messageRepository, Mockito.times(1)).save(ArgumentMatchers.any(Message.class));
        Mockito.verify(messageRepository, Mockito.times(1)).findById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testPutWithIdNullMessageBadRequest() {
        Message requestMess =
                Message.builder().
                        id(null).
                        message(messages.get(0).getMessage().concat("test!")).
                        build();
        webClient.put().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(requestMess)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isBadRequest()
                ).expectBody(String.class).isEqualTo("id == null");
        Mockito.verify(messageRepository, Mockito.times(0)).save(ArgumentMatchers.any(Message.class));
        Mockito.verify(messageRepository, Mockito.times(0)).findById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testPutNotFoundMessage() throws JsonProcessingException {
        Mockito.when(messageRepository.findById(ArgumentMatchers.any(Long.class)))
                .thenReturn(Mono.empty());
        Message requestMess =
                Message.builder().
                        id(Long.MAX_VALUE).
                        message(messages.get(0).getMessage().concat("test!")).
                        build();
        webClient.put().
                uri("/message")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(requestMess)
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isNotFound()
                ).expectBody().isEmpty();
        Mockito.verify(messageRepository, Mockito.times(0)).save(ArgumentMatchers.any(Message.class));
        Mockito.verify(messageRepository, Mockito.times(1)).findById(ArgumentMatchers.any(Long.class));
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
        Mockito.verify(messageRepository, Mockito.times(1)).findById(ArgumentMatchers.any(Long.class));
        Mockito.verify(messageRepository, Mockito.times(1)).deleteById(ArgumentMatchers.any(Long.class));
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
        Mockito.verify(messageRepository, Mockito.times(1)).findById(ArgumentMatchers.any(Long.class));
        Mockito.verify(messageRepository, Mockito.times(0)).deleteById(ArgumentMatchers.any(Long.class));
    }

    @Test
    public void testPostMultipartFile() throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        File fileSaveJpg = File.builder().
                id(1L).
                partFileName(FILE_NAME_IMG_JPG).
                generalFileName("mainTestFile").
                file(this.getClass().getResourceAsStream("/" + FILE_NAME_IMG_JPG).readAllBytes()).build();
        File fileSaveSmallJpg = File.builder().
                id(2L).
                partFileName(FILE_NAME_IMG_SMALL_JPG).
                generalFileName("mainTestFile").
                file(this.getClass().getResourceAsStream("/" + FILE_NAME_IMG_SMALL_JPG).readAllBytes()).build();
        builder.part("name_will_be_replaced!", fileSaveJpg.getFile())
                .header("Content-Disposition",
                        //name=%s will rewrite builder.part argument-"name".
                        //"name" is general file name which consists of "filename"(it's name of part-file)
                        String.format("form-data; name=%s; filename=%s",
                                fileSaveJpg.getGeneralFileName(),
                                fileSaveJpg.getPartFileName()));
        builder.part("another_name_will_be_replaced!", fileSaveSmallJpg.getFile())
                .header("Content-Disposition",
                        String.format("form-data; name=%s; filename=%s",
                                fileSaveSmallJpg.getGeneralFileName(),
                                fileSaveSmallJpg.getPartFileName()));
        builder.part("test_another_property", "test_value");

        Mockito.when(fileRepository.save(ArgumentMatchers.any(File.class)))
                .thenReturn(Mono.just(fileSaveJpg));
        Mockito.when(fileRepository.createWithId(ArgumentMatchers.any(File.class)))
                .thenReturn(Mono.just(fileSaveSmallJpg));

        webClient.post().
                uri("/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody(String.class).isEqualTo("ok");

        Mockito.verify(fileRepository, Mockito.times(1)).
                save(ArgumentMatchers.any(File.class));
        Mockito.verify(fileRepository, Mockito.times(1)).
                createWithId(ArgumentMatchers.any(File.class));
    }

    @Test
    public void testGetMultipartFile() throws IOException {
        byte[] file = this.getClass().getResourceAsStream("/" + FILE_NAME_IMG_JPG).readAllBytes();
        File fileSave = File.builder().id(1L).partFileName(FILE_NAME_IMG_JPG).generalFileName("test").file(file).build();
        Mockito.when(fileRepository.getFileByIdAndPartFileName(ArgumentMatchers.any(Long.class),
                        ArgumentMatchers.any(String.class)))
                .thenReturn(Mono.just(fileSave));

        webClient.get().
                uri("/file/{id_file}/{file_name}", fileSave.getId(), fileSave.getPartFileName())
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody(byte[].class).isEqualTo(file);
        Mockito.verify(fileRepository, Mockito.times(1)).
                getFileByIdAndPartFileName(fileSave.getId(), fileSave.getPartFileName());
    }
}
