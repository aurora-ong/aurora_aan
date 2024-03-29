package ong.aurora.aan.projector.ksa_projector;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.entity.AANEntity;
import ong.aurora.aan.entity.EntityValue;
import ong.aurora.aan.event.Event;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class KSAProjectorRestService {
    private final HostInfo hostInfo;
    private final KafkaStreams streams;

    private final List<AANEntity> entity2List;

    private static final Logger log = LoggerFactory.getLogger(KSAProjectorRestService.class);

    KSAProjectorRestService(HostInfo hostInfo, KafkaStreams streams, List<AANEntity> entity2List) {
        this.entity2List = entity2List;
        this.hostInfo = hostInfo;
        this.streams = streams;
    }

    void start() {
        Javalin app = Javalin.create().start(hostInfo.port());


        for (AANEntity entity2 : this.entity2List) {
            String allPath = "/".concat(entity2.entityName).concat("/all");
            String onePath = "/".concat(entity2.entityName).concat("/one");
            String tracePath = "/".concat(entity2.entityName).concat("/trace");
            log.info("Registrando ruta {}", allPath);
            app.get(allPath, ctx -> this.entityAll(ctx, entity2));
            log.info("Registrando ruta {}", onePath);
            app.get(onePath, ctx -> this.entityOne(ctx, entity2));
            log.info("Registrando ruta {}", tracePath);
            app.get(tracePath, ctx -> this.entityTrace(ctx, entity2));
        }

        app.get("/event/all", this::eventAll);

    }

    <K, V extends EntityValue<V>> void entityAll(Context context, AANEntity<K, V> entity) {

        try {

            List<MaterializedEntity<V>> entityList = new ArrayList<>();

            ReadOnlyKeyValueStore<K, MaterializedEntity<V>> keyValueStore = streams.store(
                    StoreQueryParameters.fromNameAndType(
                            "projector-".concat(entity.entityName).concat("-table"), QueryableStoreTypes.keyValueStore()));

            try (KeyValueIterator<K, MaterializedEntity<V>> iterator = keyValueStore.all()) {
                while (iterator.hasNext()) {
                    KeyValue<K, MaterializedEntity<V>> keyValue = iterator.next();
                    log.info("Encontrado {}", keyValue);
                    entityList.add(keyValue.value);
                }
            }

            context.json(entityList);

        } catch (InvalidStateStoreException e) {
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            // ENVIAR ERROR
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.BAD_REQUEST);
            throw e;
        }



    }

    <K, V extends EntityValue<V>> void entityOne(Context context, AANEntity<K, V> entity) {


        ReadOnlyKeyValueStore<K, MaterializedEntity<V>> keyValueStore = streams.store(
                StoreQueryParameters.fromNameAndType(
                        "projector-".concat(entity.entityName).concat("-table"), QueryableStoreTypes.keyValueStore()));


        try {
            K key = context.bodyAsClass(entity.keyType);
            String body = context.body();
            log.info("ENTITY GET DATA {}", body);
            log.info("ENTITY GET DATA {}", key);
            MaterializedEntity<V> value = keyValueStore.get(key);
            if (value == null) {
                context.status(HttpCode.NOT_FOUND);
            } else {
                context.json(value);
            }

        } catch (InvalidStateStoreException e) {
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            // ENVIAR ERROR
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.BAD_REQUEST);
            throw e;
        }

    }

    <K, V extends EntityValue<V>> void entityTrace(Context context, AANEntity<K, V> entity) {

        ReadOnlyKeyValueStore<K, List<Event>> keyValueStore = streams.store(
                StoreQueryParameters.fromNameAndType(
                        "projector-".concat(entity.entityName).concat("-trace-table"), QueryableStoreTypes.keyValueStore()));


        try {
            K key = context.bodyAsClass(entity.keyType);
            String body = context.body();
            log.info("ENTITY GET DATA {}", body);
            log.info("ENTITY GET DATA {}", key);
            List<Event> value = keyValueStore.get(key);
            if (value == null) {
                context.status(HttpCode.NOT_FOUND);
            } else {
                context.json(value);
            }

        } catch (InvalidStateStoreException e) {
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            // ENVIAR ERROR
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.BAD_REQUEST);
            throw e;
        }

    }


    ReadOnlyKeyValueStore<Long, Event> getEventStore() {
        return streams.store(
                StoreQueryParameters.fromNameAndType(
                        "aan-events-store", QueryableStoreTypes.keyValueStore()));
    }

    void eventAll(Context context) {

        try {

            List<Event> eventList = new ArrayList<>();
            try (KeyValueIterator<Long, Event> iterator = this.getEventStore().all()) {
                while (iterator.hasNext()) {
                    KeyValue<Long, Event> keyValue = iterator.next();
                    eventList.add(keyValue.value);
                }
            }

            context.json(eventList);

        } catch (InvalidStateStoreException e) {
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            // ENVIAR ERROR
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.BAD_REQUEST);
            throw e;
        }



    }

}
