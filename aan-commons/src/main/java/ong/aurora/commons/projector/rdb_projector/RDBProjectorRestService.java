package ong.aurora.commons.projector.rdb_projector;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;
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
import java.util.Optional;

class RDBProjectorRestService {
    private final HostInfo hostInfo;


    private final List<AANEntity> entity2List;

    RDBProjector projector;

    private static final Logger log = LoggerFactory.getLogger(RDBProjectorRestService.class);

    RDBProjectorRestService(HostInfo hostInfo, RDBProjector projector, List<AANEntity> entityList) {
        this.entity2List = entityList;
        this.hostInfo = hostInfo;
        this.projector = projector;
    }

    void start() {
        Javalin app = Javalin.create().start(hostInfo.port());


        for (AANEntity aanEntity : this.entity2List) {
            String allPath = "/".concat(aanEntity.entityName).concat("/all");
            String onePath = "/".concat(aanEntity.entityName).concat("/one");
            String tracePath = "/".concat(aanEntity.entityName).concat("/trace");
            log.info("Registrando ruta {}", allPath);
            app.get(allPath, ctx -> this.entityAll(ctx, aanEntity));
            log.info("Registrando ruta {}", onePath);
            app.get(onePath, ctx -> this.entityOne(ctx, aanEntity));
            log.info("Registrando ruta {}", tracePath);
            app.get(tracePath, ctx -> this.entityTrace(ctx, aanEntity));
        }

        app.get("/event/all", this::eventAll);

    }

    <K, V extends EntityValue<V>> void entityAll(Context context, AANEntity<K, V> entity) {

        try {
            context.json(this.projector.queryAll(entity));
        } catch (Exception e) {
            // ENVIAR ERROR
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.BAD_REQUEST);
            throw e;
        } catch (CommandProjectorQueryException e) {
            log.error("CommandProjectorQueryException {}", e);
            context.status(HttpCode.BAD_REQUEST);
        }

    }

    <K, V extends EntityValue<V>> void entityOne(Context context, AANEntity<K, V> entity) {

        try {
            K key = context.bodyAsClass(entity.keyType);
            String body = context.body();
            log.info("ENTITY GET DATA {}", body);
            log.info("ENTITY GET DATA {}", key);
            Optional<MaterializedEntity<V>> value = this.projector.queryOne(entity, key);

            if (value.isEmpty()) {
                context.status(HttpCode.NOT_FOUND);
            } else {
                context.json(value.get());
            }

        } catch (Exception e) {
            // ENVIAR ERROR
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.BAD_REQUEST);
            throw e;
        } catch (CommandProjectorQueryException e) {
            throw new RuntimeException(e);
        }

    }

    <K, V extends EntityValue<V>> void entityTrace(Context context, AANEntity<K, V> entity) {

        try {
            K key = context.bodyAsClass(entity.keyType);
            String body = context.body();
            log.info("ENTITY GET DATA {}", body);
            log.info("ENTITY GET DATA {}", key);
            List<Event> value = this.projector.traceOne(entity, key);

            if (value.isEmpty()) {
                context.status(HttpCode.NOT_FOUND);
            } else {
                context.json(value);
            }

        } catch (Exception e) {
            // ENVIAR ERROR
            log.error("Error al procesar entrada {}", e);
            context.status(HttpCode.BAD_REQUEST);
            throw e;
        } catch (CommandProjectorQueryException e) {
            throw new RuntimeException(e);
        }

    }

    void eventAll(Context context) {

//        try {
//
//            List<Event> eventList = new ArrayList<>();
//            try (KeyValueIterator<Long, Event> iterator = this.getEventStore().all()) {
//                while (iterator.hasNext()) {
//                    KeyValue<Long, Event> keyValue = iterator.next();
//                    eventList.add(keyValue.value);
//                }
//            }
//
//            context.json(eventList);
//
//        } catch (InvalidStateStoreException e) {
//            log.error("Error al procesar entrada {}", e);
//            context.status(HttpCode.SERVICE_UNAVAILABLE);
//        } catch (Exception e) {
//            // ENVIAR ERROR
//            log.error("Error al procesar entrada {}", e);
//            context.status(HttpCode.BAD_REQUEST);
//            throw e;
//        }

            context.status(HttpCode.SERVICE_UNAVAILABLE);

    }

}
