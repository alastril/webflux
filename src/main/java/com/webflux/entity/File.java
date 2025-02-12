package com.webflux.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


/*
@EmbeddedId and @Embedded not support by current version of "r2dbc-mysql"
 */
@Table(name = "files")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class File {
    @Id
    private Long id;

    //Part of business id
    private String partFileName;
    //Part of business id
    private String generalFileName;

    private byte[] fileBytes;
    private String transactionId;
}