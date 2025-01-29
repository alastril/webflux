package com.webflux.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilesExceptionHistory {
    @Id
    private Long id;
    private String userName;
    private String partFileName;
    private String generalFileName;
    private String exceptionMessage;
    private String transactionId;
}
