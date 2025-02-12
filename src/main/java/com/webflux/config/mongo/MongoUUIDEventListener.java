package com.webflux.config.mongo;

import com.webflux.entity.mongo.FilesExceptionHistoryMongo;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import java.util.UUID;

public class MongoUUIDEventListener extends AbstractMongoEventListener<FilesExceptionHistoryMongo> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<FilesExceptionHistoryMongo> event) {
        super.onBeforeConvert(event);
        FilesExceptionHistoryMongo filesExceptionHistoryMongo = event.getSource();
        filesExceptionHistoryMongo.setId(UUID.randomUUID());
    }
}
