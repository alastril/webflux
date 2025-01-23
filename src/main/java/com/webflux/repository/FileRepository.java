package com.webflux.repository;

import com.webflux.entity.File;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface FileRepository extends ReactiveCrudRepository<File, Long> {

    Mono<File> getFileByIdAndPartFileName(Long id, String partFileName);
    @Query("INSERT INTO files (id, part_file_name, general_file_name, file) VALUES (:#{#file.id}, :#{#file.partFileName}, :#{#file.generalFileName}, :#{#file.file})")
    Mono<File> createWithId(@Param("file") File file);
}
