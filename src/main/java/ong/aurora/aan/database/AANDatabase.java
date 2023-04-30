package ong.aurora.aan.database;

import ong.aurora.aan.event.Event;

import java.util.concurrent.CompletableFuture;

public interface AANDatabase {

    CompletableFuture<Boolean> persistEvent(Event event);
}
