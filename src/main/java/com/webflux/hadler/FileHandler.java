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
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
@Profile("route")
public class FileHandler {

    public static final Logger LOGGER = LogManager.getLogger(FileHandler.class);

    public static final String FILE = "file";
    @Autowired
    private FileRepository fileRepository;

    public Mono<ServerResponse> saveMultiPartFile(ServerRequest serverRequest) {
        return serverRequest.body(BodyExtractors.toMultipartData()).flatMap(parts -> {
            Set<File> saveFiles = new HashSet<>();
            parts.forEach((partName, value) -> {
                if (isListFile(value)) {
                    // Handle file
                    value.forEach(f -> {
                        try {
                            File fileSave = File.builder().build();
                            fileSave.setPartFileName(f.headers().getContentDisposition().getFilename());
                            fileSave.setGeneralFileName(f.headers().getContentDisposition().getName());
                            byte[] bytesFile = DataBufferUtils.join(f.content())
                                    .map(dataBuffer -> {
                                        try {
                                            return dataBuffer.asInputStream().readAllBytes();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).toFuture().get();
                            fileSave.setFile(bytesFile);
                            saveFiles.add(fileSave);
                            LOGGER.info("Get bytes from file success! => {}", fileSave.getPartFileName());
                        } catch (InterruptedException | ExecutionException e) {
                            LOGGER.error("Exception: {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
            if (!saveFiles.isEmpty()) {
                fileRepository.saveAll(saveFiles).subscribe();
                return ServerResponse.ok().bodyValue("ok");
            } else {
                return Mono.empty(); }

        }).switchIfEmpty(ServerResponse.badRequest().build());
    }

    private boolean isListFile(List<?> listFile) {
        return listFile.size() == listFile.stream().filter(obj -> obj instanceof FilePart).toList().size();

    }
    public Mono<ServerResponse> getFile(ServerRequest serverRequest) {
        Mono<File> fileMono = fileRepository.findById(Long.valueOf(serverRequest.pathVariable("id")));
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
