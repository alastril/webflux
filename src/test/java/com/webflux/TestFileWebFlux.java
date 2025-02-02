package com.webflux;

import com.webflux.entity.File;
import com.webflux.entity.mongo.FilesExceptionHistoryMongo;
import com.webflux.repository.mongo.FilesExceptionHistoryMongoRepository;
import com.webflux.repository.mysql.FileRepository;
import com.webflux.util.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@ActiveProfiles(profiles = {"route", "test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient //for webFlux web-client
@TestInstance(TestInstance.Lifecycle.PER_CLASS)// need for @BeforeAll - annotation if no need static
public class TestFileWebFlux {

    private static final String FILE_NAME_IMG_JPG = "test_img.jpg";
    private static final String FILE_NAME_IMG_SMALL_JPG = "test_jpg_small.jpg";

    @MockitoBean
    private FileRepository fileRepository;

    @MockitoBean
    private FilesExceptionHistoryMongoRepository filesExceptionHistoryMongoRepository;

    @MockitoBean
    private Utils utils;

    @Autowired
    private WebTestClient webClient;


    @Test
    public void testPostMultipartFile() throws IOException {
        String uuid = UUID.randomUUID().toString();
        Mockito.when(utils.generateUUID()).thenReturn(uuid);
        List<File> files = getTestFiles(uuid);
        MultipartBodyBuilder builder = getMultipartBodyBuilder(files);
        File fileSaveJpg = files.get(0);
        File fileSaveSmallJpg = files.get(1);
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
                ).expectBody(String.class).isEqualTo("transaction_id for check status:\n" + uuid);

        Mockito.verify(fileRepository, Mockito.times(2)).
                save(ArgumentMatchers.any(File.class));
        Mockito.verify(utils, Mockito.times(1)).
                generateUUID();
    }

    @Test
    public void testPostMultipartDuplicateFile() throws IOException {
        String uuid = UUID.randomUUID().toString();
        Mockito.when(utils.generateUUID()).thenReturn(uuid);
        List<File> files = getTestFiles(uuid);
        MultipartBodyBuilder builder = getMultipartBodyBuilder(files);

        Mockito.when(fileRepository.save(ArgumentMatchers.any(File.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("testError")));
        Mockito.when(filesExceptionHistoryMongoRepository.save(ArgumentMatchers.any(FilesExceptionHistoryMongo.class)))
                .thenReturn(Mono.empty());
        webClient.post().
                uri("/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody(String.class).isEqualTo("transaction_id for check status:\n" + uuid);

        Mockito.verify(fileRepository, Mockito.times(2)).
                save(ArgumentMatchers.any(File.class));
        Mockito.verify(filesExceptionHistoryMongoRepository, Mockito.times(2)).
                save(ArgumentMatchers.any(FilesExceptionHistoryMongo.class));
        Mockito.verify(utils, Mockito.times(1)).
                generateUUID();
    }

    @Test
    public void testGetMultipartFile() throws IOException {
        byte[] file = this.getClass().getResourceAsStream("/" + FILE_NAME_IMG_JPG).readAllBytes();
        File fileSave = File.builder().id(1L).partFileName(FILE_NAME_IMG_JPG).generalFileName("test").file(file).build();
        Mockito.when(fileRepository.getFileByGeneralFileNameAndPartFileName(ArgumentMatchers.any(String.class),
                        ArgumentMatchers.any(String.class)))
                .thenReturn(Mono.just(fileSave));

        webClient.get().
                uri("/file/{gen_file_name}/{part_file_name}", fileSave.getGeneralFileName(), fileSave.getPartFileName())
                .exchange()
                .expectAll(
                        responseSpec -> responseSpec.expectStatus().isOk()
                ).expectBody(byte[].class).isEqualTo(file);
        Mockito.verify(fileRepository, Mockito.times(1)).
                getFileByGeneralFileNameAndPartFileName(fileSave.getGeneralFileName(), fileSave.getPartFileName());
    }

    private MultipartBodyBuilder getMultipartBodyBuilder(List<File> files) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        File fileSaveJpg = files.get(0);
        File fileSaveSmallJpg = files.get(1);
        builder.part("name_will_be_replaced!", files.get(0).getFile())
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
        return builder;
    }

    private List<File> getTestFiles(String uuid) throws IOException {
        File fileSaveJpg = File.builder().
                id(1L).
                partFileName(FILE_NAME_IMG_JPG).
                generalFileName("mainTestFile").
                transactionId(uuid).
                file(this.getClass().getResourceAsStream("/" + FILE_NAME_IMG_JPG).readAllBytes()).build();
        File fileSaveSmallJpg = File.builder().
                id(2L).
                partFileName(FILE_NAME_IMG_SMALL_JPG).
                generalFileName("mainTestFile").
                transactionId(uuid).
                file(this.getClass().getResourceAsStream("/" + FILE_NAME_IMG_SMALL_JPG).readAllBytes()).build();
        return Arrays.asList(fileSaveJpg, fileSaveSmallJpg);
    }
}
