package ong.aurora.projector;

import com.fasterxml.jackson.databind.type.TypeFactory;
import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.serialization.JsonSerdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class EntityProjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityProjector.class);


    public static <K, V extends EntityValue<V>> void configureBranch(AANEntity<K, V> entity, Map<String, KStream<String, Event>> branchedStream) {
        LOGGER.info("Configurando entidad {}", entity.entityName);

        KStream<String, Event> createStream = branchedStream.get("projector-".concat(entity.entityName));

        createStream.print(Printed.<String, Event>toSysOut().withLabel("projector-".concat(entity.entityName)));

        KStream<K, MaterializedEntity<V>> entityStream =
                createStream.map(
                        (key, event) -> {
                            LOGGER.debug("Serializando {} {}", entity.entityName, event.eventData());
                            return KeyValue.pair(entity.keyFromEvent(event), entity.materializeFromEvent(event));
                        });




//        LOGGER.info("Entity key {} value {}", entity.entityKey(), entity.entityValue());
        LOGGER.info("Entity key {} value {}", entity.keyType, entity.valueType);

        KGroupedStream<K, MaterializedEntity<V>> groupedStream = entityStream.groupByKey(Grouped.with(JsonSerdes.getJSONSerde(entity.keyType), JsonSerdes.getJSONSerde(TypeFactory.defaultInstance().constructParametricType(MaterializedEntity.class, entity.valueType))));

        KStream<K, MaterializedEntity<V>> reduceKStream = groupedStream.reduce((value1, value2) -> {
            LOGGER.info("Procesando datos en reduce old: {} new: {}", value1, value2);


            MaterializedEntity<V> updatedValue = value1.onUpdateValue(value2);
            LOGGER.info("Procesando datos en reduce updated: {}", updatedValue);
            return updatedValue;

        }).toStream();

        reduceKStream.print(Printed.<K, MaterializedEntity<V>>toSysOut().withLabel("projector-".concat(entity.entityName).concat("-reduce")));

        final KTable<K, MaterializedEntity<V>> entityKTable = reduceKStream.
                toTable(Materialized.<K, MaterializedEntity<V>, KeyValueStore<Bytes, byte[]>>
                                as("projector-".concat(entity.entityName).concat("-table")).
                        withKeySerde(JsonSerdes.getJSONSerde(entity.keyType)).
                        withValueSerde(JsonSerdes.getJSONSerde(TypeFactory.defaultInstance().constructParametricType(MaterializedEntity.class, entity.valueType))));


        // TRACE ENTITY
        KStream<K, List<Event>> entityStream2 =
                createStream.map(
                        (key, event) -> {
                            LOGGER.debug("Serializando {} {}", entity.entityName, event.eventData());
                            return KeyValue.pair(entity.keyFromEvent(event), List.of(event));
                        });

        KGroupedStream<K, List<Event>> groupedStream2 = entityStream2.groupByKey(Grouped.with(JsonSerdes.getJSONSerde(entity.keyType), JsonSerdes.getJSONSerde(TypeFactory.defaultInstance().constructCollectionType(List.class, Event.class))));

        KStream<K, List<Event>> reduceKStream2 = groupedStream2.reduce((value1, value2) -> {
            LOGGER.info("Procesando datos en reduce trace");

            value1.addAll(value2);
            return value1;
        }).toStream();

        final KTable<K, List<Event>> traceKTable2 = reduceKStream2.
                toTable(Materialized.<K, List<Event>, KeyValueStore<Bytes, byte[]>>
                                as("projector-".concat(entity.entityName).concat("-trace-table")).
                        withKeySerde(JsonSerdes.getJSONSerde(entity.keyType)).
                        withValueSerde(JsonSerdes.getJSONSerde(TypeFactory.defaultInstance().constructCollectionType(List.class, Event.class))));


    }
}
