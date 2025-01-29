package com.webflux.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ResponseFileExceptionHistory {
    private String partFileName;
    private String generalFileName;
    private String exceptionMessage;
}
