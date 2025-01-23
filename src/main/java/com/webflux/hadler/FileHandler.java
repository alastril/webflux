package com.webflux.hadler;

import com.webflux.entity.File;
import com.webflux.repository.FileRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile("route")
public class FileHandler {

    public static final Logger LOGGER = LogManager.getLogger(FileHandler.class);

    @Autowired
    private FileRepository fileRepository;

    public Mono<ServerResponse> saveMultiPartFile(ServerRequest serverRequest) {
        return serverRequest.body(BodyExtractors.toMultipartData()).flatMap(parts -> {
            Flux<File> saveFiles = Flux.fromIterable(getSetFilesFromParts(parts));
            AtomicLong id = new AtomicLong(0);
            saveFiles.flatMap(f -> {
                //save remains files with actual id
                if (id.get() != 0) {
                    f.setId(id.get());
                    fileRepository.createWithId(f).subscribe();
                } else {
                    //save first file and get id for composite id
                    try {
                        id.set(fileRepository.save(f).toFuture().get().getId());
                    } catch (Exception e) {
                        LOGGER.error("Exception saving file, name:\n {} , \n Error: {}",
                                f.getPartFileName(), e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
                return Mono.just(f);
            }).subscribe();

            return ServerResponse.ok().bodyValue("ok");
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

        Long id = Long.valueOf(serverRequest.pathVariable("id_file"));
        String fileName = serverRequest.pathVariable("file_name");

        Mono<File> fileMono = fileRepository.getFileByIdAndPartFileName(id, fileName);
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
}
