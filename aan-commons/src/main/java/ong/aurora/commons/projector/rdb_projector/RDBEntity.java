package ong.aurora.commons.projector.rdb_projector;

import com.fasterxml.jackson.databind.type.TypeFactory;
import ong.aurora.commons.config.AANConfig;
import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.serialization.AANSerializer;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RDBEntity<K, V extends EntityValue<V>> {

    RocksDB entityDB;

    AANEntity<K, V> entity;

    AANSerializer annSerializer;

    public RDBEntity(AANEntity<K, V> entity, RocksDB rocksDB, AANSerializer annSerializer, AANConfig aanConfig) throws IOException, RocksDBException {
        this.annSerializer = annSerializer;
        this.entity = entity;

        String basePath = "/tmp/aan".concat("_").concat(aanConfig.nodeId).concat("/");
        final Options options = new Options();
        options.setCreateIfMissing(true);


        File baseDir = new File(basePath, entity.entityName);

        Files.createDirectories(baseDir.getParentFile().toPath());
        Files.createDirectories(baseDir.getAbsoluteFile().toPath());
        RocksDB.destroyDB(baseDir.getAbsolutePath(), options);
        entityDB = RocksDB.open(options, baseDir.getAbsolutePath());


    }

    public void processEvent(Event event) throws RocksDBException {
        K key = entity.keyFromEvent(event);

        V value = entity.valueFromEvent(event);

        Optional<byte[]> rawData = Optional.ofNullable(entityDB.get(annSerializer.toBytes(key)));

        if (rawData.isPresent()) {

            RDBEntityStore<V> entityStore = annSerializer.fromBytes(rawData.get(), TypeFactory.defaultInstance().constructParametricType(RDBEntityStore.class, entity.valueType));
            MaterializedEntity<V> oldValue = entityStore.materializedEntity();
            V valueUpdated = oldValue.getEntityValue().onUpdateValue(value);
            List<Event> events = new ArrayList<>(entityStore.eventList());
            events.add(event);
            RDBEntityStore<V> entityStoreUpdated = new RDBEntityStore<>(events, new MaterializedEntity<>(valueUpdated, oldValue.getCreatedAt(), event.eventTimestamp()));
            entityDB.put(annSerializer.toBytes(key), annSerializer.toBytes(entityStoreUpdated));
        }

        if (rawData.isEmpty()) {
            RDBEntityStore<V> entityStore = new RDBEntityStore<>(List.of(event), new MaterializedEntity<>(value, event.eventTimestamp(), event.eventTimestamp()));
            entityDB.put(annSerializer.toBytes(key), annSerializer.toBytes(entityStore));
        }


    }

    public List<MaterializedEntity<V>> getAll() throws RocksDBException {
        ArrayList<MaterializedEntity<V>> valueList = new ArrayList<>();
        RocksIterator iterator = this.entityDB.newIterator();
        iterator.seekToFirst();
        while (iterator.isValid()) {
            RDBEntityStore<V> value = annSerializer.fromBytes(iterator.value(), TypeFactory.defaultInstance().constructParametricType(RDBEntityStore.class, entity.valueType));
            valueList.add(value.materializedEntity());
            iterator.next();
        }
        iterator.close();
        return valueList;
    }

    public Optional<MaterializedEntity<V>> getOne(K key) throws RocksDBException {


        Optional<byte[]> rawData = Optional.ofNullable(entityDB.get(annSerializer.toBytes(key)));

        if (rawData.isEmpty()) {
            return Optional.empty();
        }

        RDBEntityStore<V> value = annSerializer.fromBytes(rawData.get(), TypeFactory.defaultInstance().constructParametricType(RDBEntityStore.class, entity.valueType));
        return Optional.of(value.materializedEntity());
    }

    public List<Event> traceEntity(K key) throws RocksDBException {

        Optional<byte[]> rawData = Optional.ofNullable(entityDB.get(annSerializer.toBytes(key)));

        if (rawData.isEmpty()) {
            return List.of();
        }

        RDBEntityStore<V> value = annSerializer.fromBytes(rawData.get(), TypeFactory.defaultInstance().constructParametricType(RDBEntityStore.class, entity.valueType));
        return value.eventList();
    }


}
