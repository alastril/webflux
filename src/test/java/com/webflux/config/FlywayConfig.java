package com.webflux.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
public class FlywayConfig {

    @Value("${spring.flyway.url}")
    private String flywayURL;
    @Value("${spring.flyway.user}")
    private String flywayUser;
    @Value("${spring.flyway.password}")
    private String flywayPassword;
    @Value("${spring.flyway.schemas}")
    private String flywaySchemas;
    @Value("${spring.flyway.locations}")
    private String flywayLocations;
}
