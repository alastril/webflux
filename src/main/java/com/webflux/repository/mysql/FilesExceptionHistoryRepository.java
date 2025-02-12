package com.webflux.repository.mysql;

import com.webflux.entity.FilesExceptionHistory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface FilesExceptionHistoryRepository  extends ReactiveCrudRepository<FilesExceptionHistory, Long> {

    Flux<FilesExceptionHistory> getFileExceptionHistoryByTransactionId(String transactionId);
}
