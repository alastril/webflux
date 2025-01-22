package com.webflux.repository;

import com.webflux.entity.File;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends ReactiveCrudRepository<File, Long> {
}
