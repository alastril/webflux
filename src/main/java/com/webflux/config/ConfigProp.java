package com.webflux.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("client")
public class ConfigProp {
    @Value("${uri.client}")
    public static String URI_CLIENT;
}
