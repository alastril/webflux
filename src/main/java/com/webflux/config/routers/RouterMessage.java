package com.webflux.config.routers;

import com.webflux.hadler.FileHandler;
import com.webflux.hadler.MessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@Profile({"mysql","mongo"})
public class RouterMessage {

    private static final String PATH_MESSAGE = "/message";
    private static final String PATH_FILE = "/file";
    private static final String PATH_FILE_HISTORY = "/file_history";

    @Bean
    public RouterFunction<ServerResponse> routerMess(MessageHandler messageHandler, FileHandler fileHandler) {
      return RouterFunctions.route(
                        GET(PATH_MESSAGE + "/{id}").and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::getMessageById).
                        andRoute(GET(PATH_MESSAGE).and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::getMessages).
                        andRoute(POST(PATH_MESSAGE).and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::saveMessage).
                        andRoute(PUT(PATH_MESSAGE).and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::updateMessage).
                        andRoute(DELETE(PATH_MESSAGE + "/{id}").and(accept(MediaType.APPLICATION_JSON)),
                                messageHandler::deleteMessage).
                        andRoute(POST(PATH_FILE).and(accept(MediaType.MULTIPART_FORM_DATA)),
                                fileHandler::saveMultiPartFile).
                        andRoute(GET(PATH_FILE + "/{gen_file_name}/{part_file_name}"),
                                fileHandler::getFile).
                        andRoute(GET(PATH_FILE_HISTORY + "/{tr_id}"),
                                fileHandler::getFileExceptionHistoryByTransactionId).
                        andRoute(GET(PATH_FILE_HISTORY + "/test/{name_user}"),
                                fileHandler::getFilesExceptionHistoryMongoByUserName);
    }
}
