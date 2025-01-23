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
    //Part of composite id
    private Long id;
    //Part of composite id
    private String partFileName;

    private String generalFileName;
    private byte[] file;
}