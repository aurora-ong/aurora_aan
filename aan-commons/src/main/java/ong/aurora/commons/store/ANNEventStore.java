package ong.aurora.commons.store;

import ong.aurora.commons.event.Event;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface ANNEventStore {

    CompletableFuture<Void> saveEvent(String event) throws IOException, Exception;

    Stream<String> readEventStore();
}
