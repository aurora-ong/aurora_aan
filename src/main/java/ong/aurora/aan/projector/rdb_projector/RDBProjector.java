package ong.aurora.aan.projector.rdb_projector;

import ong.aurora.aan.command.CommandProjectorQueryException;
import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.config.AANConfig;
import ong.aurora.aan.entity.AANEntity;
import ong.aurora.aan.entity.EntityValue;
import ong.aurora.aan.event.Event;
import ong.aurora.aan.model.AANModel;
import ong.aurora.aan.node.AANNodeEntity;
import ong.aurora.aan.projector.AANProjector;
import ong.aurora.aan.serialization.AANSerializer;
import org.apache.kafka.streams.state.HostInfo;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RDBProjector implements AANProjector {

    private static final Logger log = LoggerFactory.getLogger(RDBProjector.class);
    RocksDB db;

    Map<String, RDBEntity> rdbEntityMap;

    AANSerializer aanSerializer;
    AANConfig aanConfig;
    AANModel aanModel;

    public RDBProjector(AANSerializer aanSerializer, AANConfig aanConfig, AANModel aanModel) {
        this.aanSerializer = aanSerializer;
        this.aanConfig = aanConfig;
        this.aanModel = aanModel;
    }

    @Override
    public CompletableFuture<Void> startProjector() throws Exception {
        RocksDB.loadLibrary();


        List<AANEntity> proyectorEntities = new ArrayList<>();

        proyectorEntities.add(new AANNodeEntity());
        proyectorEntities.addAll(aanModel.getModelEntities());

        rdbEntityMap = proyectorEntities.stream().collect(Collectors.toMap(entity -> entity.entityName, entity -> {
            try {
                return new RDBEntity(entity, db, this.aanSerializer, aanConfig);
            } catch (IOException | RocksDBException e) {
                log.error("Error initializng RocksDB. Exception: '{}', message: '{}'", e.getCause(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }));

        log.info("RocksDB initialized");
        HostInfo hostInfo = new HostInfo("localhost", aanConfig.projectorPort);
        RDBProjectorRestService projectorRestService = new RDBProjectorRestService(hostInfo, this, proyectorEntities);
        projectorRestService.start();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> projectEvent(Event event) throws Exception {

        log.info("Projectando evento {} con entidad '{}'", event.eventId(), event.eventName());

        if (!this.rdbEntityMap.containsKey(event.eventName())) {
            throw new Exception("Entity no encontrado");
        }

        this.rdbEntityMap.get(event.eventName()).processEvent(event);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <K, V extends EntityValue<V>> List<MaterializedEntity<V>> queryAll(AANEntity<K, V> entity) throws CommandProjectorQueryException {
        if (!this.rdbEntityMap.containsKey(entity.entityName)) {
            throw new CommandProjectorQueryException("Entity no encontrado");
        }

        try {
            return this.rdbEntityMap.get(entity.entityName).getAll();
        } catch (RocksDBException e) {
            throw new CommandProjectorQueryException("exception ");
        }
    }

    @Override
    public <K, V extends EntityValue<V>> Optional<MaterializedEntity<V>> queryOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException {

        if (!this.rdbEntityMap.containsKey(entity.entityName)) {
            throw new CommandProjectorQueryException("Entity no encontrado");
        }

        try {
            return this.rdbEntityMap.get(entity.entityName).getOne(id);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
//        return Optional.empty();
    }

    @Override
    public <K, V extends EntityValue<V>> List<Event> traceOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException {

        if (!this.rdbEntityMap.containsKey(entity.entityName)) {
            throw new CommandProjectorQueryException("Entity no encontrado");
        }

        try {
            return this.rdbEntityMap.get(entity.entityName).traceEntity(id);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
//        return Optional.empty();
    }
}

