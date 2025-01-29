package com.webflux.hadler;

import com.webflux.response.ResponseFileExceptionHistory;
import com.webflux.util.Utils;
import com.webflux.entity.File;
import com.webflux.entity.FilesExceptionHistory;
import com.webflux.repository.FileRepository;
import com.webflux.repository.FilesExceptionHistoryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
@Profile("route")
public class FileHandler {

    public static final Logger LOGGER = LogManager.getLogger(FileHandler.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private Utils utils;

    @Autowired
    private FilesExceptionHistoryRepository filesExceptionHistoryRepository;

    public Mono<ServerResponse> saveMultiPartFile(ServerRequest serverRequest) {
        String transactionId = utils.generateUUID();
        return serverRequest.body(BodyExtractors.toMultipartData()).flatMap(parts -> {
            getSetFilesFromParts(parts).forEach( file -> {
                fileRepository.save(file).
                        onErrorResume( e -> {
                            if (e instanceof DuplicateKeyException) {
                                LOGGER.info("Error on save file => {} error =>{}", file.getPartFileName(), e.getLocalizedMessage());
                                filesExceptionHistoryRepository.save(FilesExceptionHistory
                                        .builder()
                                                .exceptionMessage("Duplicate PartName and GeneralName!")
                                                .generalFileName(file.getGeneralFileName())
                                                .partFileName(file.getPartFileName())
                                                .userName("Root")
                                                .transactionId(transactionId)
                                        .build()).subscribe();
                            }
                            return Mono.empty();
                        }).subscribe();
            });
            return ServerResponse.ok().bodyValue("transaction_id for check status:\n" + transactionId);
        }).switchIfEmpty(ServerResponse.badRequest().build());
    }

    private boolean isListFile(List<?> listFile) {
        return listFile.size() == listFile.stream().filter(obj -> obj instanceof FilePart).count();
    }

    private Set<File> getSetFilesFromParts(MultiValueMap<String, Part> multiValueMap) {
        Set<File> saveFiles = new HashSet<>();
        multiValueMap.forEach((partName, value) -> {
            if (isListFile(value)) {
                // Handle and save file to set
                value.forEach(f -> {
                    try {
                        byte[] bytesFile = DataBufferUtils.join(f.content())
                                .map(dataBuffer -> {
                                    try {
                                        return dataBuffer.asInputStream().readAllBytes();
                                    } catch (IOException e) {
                                        LOGGER.error("Exception on reading bytes: {}", e.getMessage());
                                        throw new RuntimeException(e);
                                    }
                                }).toFuture().get();
                        File fileSave = File.builder()
                                .partFileName(f.headers().getContentDisposition().getFilename())
                                .generalFileName(f.headers().getContentDisposition().getName())
                                .file(bytesFile)
                                .build();
                        saveFiles.add(fileSave);
                        LOGGER.info("Get bytes from file success! => {}", fileSave.getPartFileName());
                    } catch (InterruptedException | ExecutionException e) {
                        LOGGER.error("Exception: {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        return saveFiles;
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
                            bodyValue(file.getFile());
                }).
                switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getFileExceptionHistoryByTransactionId(ServerRequest serverRequest) {
        String transactionId = serverRequest.pathVariable("tr_id");
        Flux<FilesExceptionHistory> filesExceptionHistoryFlux =
                filesExceptionHistoryRepository.getFileExceptionHistoryByTransactionId(transactionId);
             Flux<ResponseFileExceptionHistory> stringBufferFlux =   filesExceptionHistoryFlux.
                map(file -> {
                    ResponseFileExceptionHistory responseFileExceptionHistory = ResponseFileExceptionHistory.builder().
                            partFileName(file.getPartFileName()).
                            generalFileName(file.getGeneralFileName()).
                            exceptionMessage(file.getExceptionMessage()).build();
                    LOGGER.debug("Get exception on file saving! => {}", responseFileExceptionHistory);
                    return responseFileExceptionHistory;
                });
        try {
            return !stringBufferFlux.hasElements().toFuture().get() ?
                    ServerResponse.ok().bodyValue("All files was saved successfully!") :
                    ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromProducer(stringBufferFlux, ResponseFileExceptionHistory.class));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
