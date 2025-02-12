package com.webflux.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFileExceptionHistory {
    private String userName;
    private String partFileName;
    private String generalFileName;
    private String exceptionMessage;
}
