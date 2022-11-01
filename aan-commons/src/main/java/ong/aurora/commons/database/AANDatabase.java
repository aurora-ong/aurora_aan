package ong.aurora.commons.database;

import ong.aurora.commons.event.Event;

import java.util.concurrent.CompletableFuture;

public interface AANDatabase {

    CompletableFuture<Boolean> persistEvent(Event event);
}
