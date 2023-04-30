package ong.aurora.aan.projector;

import ong.aurora.aan.command.CommandProjectorQueryException;
import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.event.Event;
import ong.aurora.aan.entity.AANEntity;
import ong.aurora.aan.entity.EntityValue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AANProjector {

    CompletableFuture<Void> startProjector() throws Exception;

    CompletableFuture<Void> projectEvent(Event event) throws Exception;

    <K, V extends EntityValue<V>> List<MaterializedEntity<V>> queryAll(AANEntity<K, V> entity) throws CommandProjectorQueryException;

    <K, V extends EntityValue<V>> Optional<MaterializedEntity<V>> queryOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException;

    <K, V extends EntityValue<V>> List<Event> traceOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException;
}
