package ong.aurora.processor.database;

import ong.aurora.commons.database.AANDatabase;
import ong.aurora.commons.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class FileDatabase implements AANDatabase {

    public FileDatabase() {
    }

    private static final Logger logger = LoggerFactory.getLogger(FileDatabase.class);

    @Override
    public CompletableFuture<Boolean> persistEvent(Event event) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        logger.info("Evento persistido {} hash {}", event.eventId(), event.blockHash());

        return CompletableFuture.completedFuture(null);
    }
}
