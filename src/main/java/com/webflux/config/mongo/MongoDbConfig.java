package com.webflux.config.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.webflux.repository.mongo", reactiveMongoTemplateRef = "mongoTemplate")
public class MongoDbConfig {

    @Value("${spring.r2dbc.mongo.url}")
    private String mongoHost;
    @Value("${spring.r2dbc.mongo.username}")
    private String mongoUser;
    @Value("${spring.r2dbc.mongo.password}")
    private String mongoPassword;
    @Value("${spring.r2dbc.mongo.schema}")
    private String mongoSchema;
    @Value("${spring.r2dbc.mongo.port}")
    private Integer mongoPort;

    public @Bean MongoClient mongoClient() {
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://"
                        + mongoUser + ":" + mongoPassword +
                        "@" + mongoHost + ":" + mongoPort))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoUUIDEventListener getMongoUUIDEventListener(){
        return new MongoUUIDEventListener();
    }


    @Bean
    public ReactiveMongoDatabaseFactory mongoClientFactoryBean(MongoClient client) {
        return new SimpleReactiveMongoDatabaseFactory(client, mongoSchema);
    }

    @Bean
    public ReactiveMongoTemplate mongoTemplate(ReactiveMongoDatabaseFactory mongoClient) {
        return new ReactiveMongoTemplate(mongoClient);
    }
}
