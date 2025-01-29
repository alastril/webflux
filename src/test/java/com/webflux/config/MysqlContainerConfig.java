package com.webflux.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;


public class MysqlContainerConfig implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Container
    public DockerComposeContainer dockerComposeContainer =
            new DockerComposeContainer( new File("src/test/resources/docker-compose-webflux-test.yml"))
            .withExposedService("mysql_1",3306, Wait.forListeningPorts(3306));


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.getBeanFactory().registerResolvableDependency(DockerComposeContainer.class, dockerComposeContainer);
        dockerComposeContainer.start();
    }
}
