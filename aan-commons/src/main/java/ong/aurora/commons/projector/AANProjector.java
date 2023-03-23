package ong.aurora.commons.projector;

import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.model.AANModel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface AANProjector {

    CompletableFuture<Void> startProjector() throws Exception;

    CompletableFuture<Void> projectEvent(Event event) throws Exception;

    <K, V extends EntityValue<V>> List<MaterializedEntity<V>> queryAll(AANEntity<K, V> entity) throws CommandProjectorQueryException;

    <K, V extends EntityValue<V>> Optional<MaterializedEntity<V>> queryOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException;

    <K, V extends EntityValue<V>> List<Event> traceOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException;
}
