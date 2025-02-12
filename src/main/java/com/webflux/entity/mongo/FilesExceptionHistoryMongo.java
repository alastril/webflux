package com.webflux.entity.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class FilesExceptionHistoryMongo {
    @Id
    private UUID id;
    private String userName;
    private String partFileName;
    private String generalFileName;
    private String exceptionMessage;
    private String transactionId;
}
