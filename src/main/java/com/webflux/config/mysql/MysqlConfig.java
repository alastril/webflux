package com.webflux.config.mysql;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.webflux.repository.mysql", entityOperationsRef = "mysqlR2dbcEntityOperations")
@Profile("mysql")
public class MysqlConfig {

    @Value("${spring.r2dbc.url}")
    private String mysqlHost;
    @Value("${spring.r2dbc.username}")
    private String mysqlUser;
    @Value("${spring.r2dbc.password}")
    private String mysqlPassword;
    @Value("${spring.r2dbc.schema}")
    private String mysqlSchema;
    @Value("${spring.r2dbc.port}")
    private Integer mysqlPort;

    @Bean
    public ConnectionFactory mysqlConnectionFactory(){
        ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.builder().
                option(ConnectionFactoryOptions.HOST, mysqlHost).
                option(ConnectionFactoryOptions.USER, mysqlUser).
                option(ConnectionFactoryOptions.PASSWORD, mysqlPassword).
                option(ConnectionFactoryOptions.DRIVER, "mysql")
                .option(ConnectionFactoryOptions.DATABASE, mysqlSchema)
                .option(ConnectionFactoryOptions.PORT, mysqlPort)
                .option(ConnectionFactoryOptions.PROTOCOL, "pipes")
                .option(ConnectionFactoryOptions.SSL, true)
                .option(Option.valueOf("createDatabaseIfNotExist"), true)
                .option(Option.valueOf("useTimezone"), true)
                .option(Option.valueOf("serverTimezone"), "UTC")
                .build();

        return ConnectionFactories.get(connectionFactoryOptions);
    }

    @Bean
    public R2dbcEntityOperations mysqlR2dbcEntityOperations(ConnectionFactory connectionFactory) {
        DatabaseClient databaseClient = DatabaseClient.create(connectionFactory);
        return new R2dbcEntityTemplate(databaseClient, MySqlDialect.INSTANCE);
    }
}
