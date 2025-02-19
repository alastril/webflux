package com.webflux.hadler;

import com.webflux.entity.mongo.FilesExceptionHistoryMongo;
import com.webflux.repository.mongo.FilesExceptionHistoryMongoRepository;
import com.webflux.response.ResponseFileExceptionHistory;
import com.webflux.response.StandardResponse;
import com.webflux.util.Utils;
import com.webflux.entity.File;
import com.webflux.repository.mysql.FileRepository;
import io.netty.util.internal.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;

@Component
@Profile({"mysql","mongo"})
@Transactional(isolation = Isolation.READ_COMMITTED)
public class FileHandler {

    public static final Logger LOGGER = LogManager.getLogger(FileHandler.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private Utils utils;

    @Autowired
    private FilesExceptionHistoryMongoRepository filesExceptionHistoryMongoRepository;

    public Mono<ServerResponse> saveMultiPartFile(ServerRequest serverRequest) {
        String transactionId = utils.generateUUID();
        return serverRequest.body(BodyExtractors.toMultipartData()).flatMap(parts -> {
            parts.forEach((partName, value) ->
                saveFileFromPart(value, transactionId)
            );
            return ServerResponse.ok().bodyValue(StandardResponse.builder().transactionId(transactionId).build());
        }).switchIfEmpty(ServerResponse.badRequest().build());
    }

    private boolean isListFile(List<?> listFile) {
        return listFile.size() == listFile.stream().filter(FilePart.class::isInstance).count();
    }

    private void saveFileFromPart(List<Part> value, String transactionId){
        if (isListFile(value)) {
            // Handle and save file to set
            value.forEach(f ->
                    getFileFromPart(f).flatMap(file -> {
                        file.setTransactionId(transactionId);
                        fileRepository.save(file).
                                onErrorResume(e -> {
                                    LOGGER.info("Error on save file => {} error =>{}", file.getPartFileName(), e.getLocalizedMessage());
                                    FilesExceptionHistoryMongo filesExceptionHistoryMongo = FilesExceptionHistoryMongo
                                            .builder()
                                            .exceptionMessage("Duplicate PartName and GeneralName!")
                                            .generalFileName(file.getGeneralFileName())
                                            .partFileName(file.getPartFileName())
                                            .userName("Root")
                                            .transactionId(transactionId)
                                            .build();
                                    if (!(e instanceof DuplicateKeyException)) {
                                        filesExceptionHistoryMongo.setExceptionMessage(e.getMessage());
                                    }
                                    filesExceptionHistoryMongoRepository.save(filesExceptionHistoryMongo).subscribe();
                                    return Mono.empty();
                                }).subscribe();
                        return Mono.empty();
                    }).subscribe()
            );
        }
    }

    private Mono<File> getFileFromPart(Part part) {
        return DataBufferUtils.join(part.content())
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = dataBuffer.asInputStream().readAllBytes();
                        DataBufferUtils.release(dataBuffer);
                        return bytes;
                    } catch (IOException e) {
                        LOGGER.error("Exception on reading bytes: {}", e.getMessage());
                        return StringUtil.EMPTY_STRING.getBytes();
                    }
                }).map(bytesFile -> {
                    File fileForSave = File.builder()
                            .partFileName(part.headers().getContentDisposition().getFilename())
                            .generalFileName(part.headers().getContentDisposition().getName())
                            .fileBytes(bytesFile)
                            .build();
                    LOGGER.info("Get bytes from file success! => {}", fileForSave.getPartFileName());
                    return fileForSave;
                });
    }

    public Mono<ServerResponse> getFile(ServerRequest serverRequest) {

        String mainFileName = serverRequest.pathVariable("gen_file_name");
        String partFileName = serverRequest.pathVariable("part_file_name");

        Mono<File> fileMono = fileRepository.getFileByGeneralFileNameAndPartFileName(mainFileName, partFileName);
        return fileMono.
                flatMap(file -> {
                    LOGGER.info("Get file name success! => {}", file.getPartFileName());
                    return ServerResponse.ok().
                            header(HttpHeaders.CONTENT_DISPOSITION,
                                    String.format("form-data; name=%s; filename=\"%s\"", file.getGeneralFileName(), file.getPartFileName())).
                            contentType(MediaType.APPLICATION_OCTET_STREAM).
                            bodyValue(file.getFileBytes());
                }).
                switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getFileExceptionHistoryByTransactionId(ServerRequest serverRequest) {
        String transactionId = serverRequest.pathVariable("tr_id");
        Flux<FilesExceptionHistoryMongo> filesExceptionHistoryFlux =
                filesExceptionHistoryMongoRepository.getFileExceptionHistoryByTransactionId(transactionId);
        return filesExceptionHistoryFlux.
                flatMap(file -> {
                    ResponseFileExceptionHistory responseFileExceptionHistory = ResponseFileExceptionHistory.builder().
                            partFileName(file.getPartFileName()).
                            generalFileName(file.getGeneralFileName()).
                            exceptionMessage(file.getExceptionMessage()).build();
                    LOGGER.debug("Get exception on file saving! =>[{}] {}", transactionId, responseFileExceptionHistory);
                    return Flux.just(responseFileExceptionHistory);
                }).collectList().
                flatMap(mono ->
                     mono.isEmpty() ?
                             ServerResponse.ok().bodyValue(StandardResponse.builder().responseText("All files was saved successfully!").build()) :
                                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(mono)
                );
    }

    public Mono<ServerResponse> getFilesExceptionHistoryMongoByUserName(ServerRequest serverRequest) {
        String nameUser = serverRequest.pathVariable("name_user");

        //1 variant
        Example<FilesExceptionHistoryMongo> filesExceptionHistoryMongoExample = Example.of(FilesExceptionHistoryMongo.builder().userName(nameUser).build());
        filesExceptionHistoryMongoRepository.findAll(filesExceptionHistoryMongoExample).
                flatMap(f -> {
                    LOGGER.info("1 variant! => {}", f);
                    return Flux.just(f);
                }).subscribe();

        //2 variant
        Flux<FilesExceptionHistoryMongo> filesExceptionHistoryFlux =
                filesExceptionHistoryMongoRepository.getFilesExceptionHistoryMongoByUserName(nameUser);
        return filesExceptionHistoryFlux.
                flatMap(file -> {
                    ResponseFileExceptionHistory responseFileExceptionHistory = ResponseFileExceptionHistory.builder().
                            partFileName(file.getPartFileName()).
                            generalFileName(file.getGeneralFileName()).
                            exceptionMessage(file.getExceptionMessage()).build();
                    LOGGER.debug("Get exception on file saving! => {}", responseFileExceptionHistory);
                    return Flux.just(responseFileExceptionHistory);
                }).collectList().
                flatMap(mono ->
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(mono)).
                switchIfEmpty(ServerResponse.ok().
                        bodyValue(StandardResponse.builder().responseText("All files was saved successfully!").build()));
    }
}
