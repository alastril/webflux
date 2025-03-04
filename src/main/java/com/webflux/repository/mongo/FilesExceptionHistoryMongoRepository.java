package com.webflux.repository.mongo;

import com.webflux.entity.mongo.FilesExceptionHistoryMongo;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface FilesExceptionHistoryMongoRepository extends ReactiveMongoRepository<FilesExceptionHistoryMongo, UUID> {

    Flux<FilesExceptionHistoryMongo> getFileExceptionHistoryByTransactionId(String transactionId);

    @Query(value = "{'userName': ?0}", fields = "{'partFileName': true, 'generalFileName': true}")
    Flux<FilesExceptionHistoryMongo> getFilesExceptionHistoryMongoByUserName (String username);
}
