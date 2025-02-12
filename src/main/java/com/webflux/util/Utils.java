package com.webflux.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@NoArgsConstructor
@Data
@Component
public class Utils {

    public String generateUUID(){
        return UUID.randomUUID().toString();
    }
}
