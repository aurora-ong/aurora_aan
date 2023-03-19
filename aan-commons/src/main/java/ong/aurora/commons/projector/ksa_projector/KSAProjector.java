package ong.aurora.commons.projector.ksa_projector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.peer.node.ANNNodeEntity;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.commons.serialization.JsonSerdes;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class KSAProjector implements AANProjector {

    private static final Logger logger = LoggerFactory.getLogger(KSAProjector.class);

    String host;

    HttpClient httpClient;

    private Producer<Long, Event> producer;


    public KSAProjector(String host, String kafkaCluster) {
        this.host = host;
        this.httpClient = HttpClient.newBuilder().build();

        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaCluster);
        props.put("acks", "all");
        this.producer = new KafkaProducer<>(props, Serdes.Long().serializer(), JsonSerdes.getJSONSerde(Event.class).serializer());

    }

    @Override
    public CompletableFuture<Void> startProjector(AANModel model) throws Exception {
        logger.info("Cargando projector-ksa");

        List<AANEntity> aanEntities = List.of(new ANNNodeEntity());

        List<AANEntity> proyectorEntities = new ArrayList<>();
        proyectorEntities.addAll(aanEntities);
        proyectorEntities.addAll(model.getModelEntities());


        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "projector-ksa");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
//        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        StreamsBuilder builder = new StreamsBuilder();


        // EVENT
        KStream<Long, Event> eventKStream =
                builder.stream(
                        "aurora-aan-events",
                        Consumed.with(Serdes.Long(), JsonSerdes.getJSONSerde(Event.class)));

        eventKStream.print(Printed.<Long, Event>toSysOut().withLabel("aurora-aan-events"));

        eventKStream.toTable(Materialized.<Long, Event, KeyValueStore<Bytes, byte[]>>as("aan-events-store").withKeySerde(Serdes.Long()).withValueSerde(JsonSerdes.getJSONSerde(Event.class)));



        BranchedKStream<Long, Event> branchedEvents2 =
                eventKStream.split(Named.as("projector-"));

        proyectorEntities.forEach(entity -> {
            Predicate<Long, Event> createPredicate =
                    (key, event) -> event.eventName().equals(entity.entityName.concat(".created"));
            branchedEvents2.branch(createPredicate, Branched.as(entity.entityName));
        });

        Map<String, KStream<Long, Event>> branchedEvents = branchedEvents2.noDefaultBranch();
        proyectorEntities.forEach(entity -> KSAProjectorEntityProjector.configureBranch(entity, branchedEvents));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Cerrando KSA Proyector..");
            streams.close();
            logger.info("Limpieza..");
            streams.cleanUp();
            logger.info("Finalizado");
        }));

        logger.info("Iniciando projector-ksa");

        CompletableFuture<Void> readyFuture = new CompletableFuture<>();

        streams.setStateListener((newState, oldState) -> {
            logger.info("State Listener: {} -> {}", oldState, newState);
            if (newState == KafkaStreams.State.RUNNING) {
                readyFuture.complete(null);
            }
        } );

        streams.start();



        HostInfo hostInfo = new HostInfo("localhost", 15002);
        logger.info("Iniciando servicio REST {}:{}", hostInfo.host(), hostInfo.port());
        KSAProjectorRestService restService = new KSAProjectorRestService(hostInfo, streams, proyectorEntities);
        restService.start();

        return readyFuture;
    }

    @Override
    public CompletableFuture<Void> projectEvent(Event event) throws Exception {

        ProducerRecord<Long, Event> newRecord = new ProducerRecord<>("aurora-aan-events", event.eventId(), event);
        this.producer.send(newRecord).get();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <K, V extends EntityValue<V>> List<MaterializedEntity<V>> queryAll(AANEntity<K, V> entity) throws CommandProjectorQueryException {
        try {
            String entityAllPath = this.host.concat("/").concat(entity.entityName).concat("/all");
            logger.info("Consultando ruta {}", entityAllPath);
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder.uri(new URI(entityAllPath));
            builder.GET();
            HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, TypeFactory.defaultInstance().constructParametricType(MaterializedEntity.class, entity.valueType));

            List<MaterializedEntity<V>> list = KSAProjector.fromJSON(response.body(), type);

            logger.info("Respuesta {}", list.toString());

            return list;

        } catch (Exception e) {
            logger.error("Error consulta", e);
            throw new CommandProjectorQueryException(e.toString());
        }

    }

    @Override
    public <K, V extends EntityValue<V>> Optional<MaterializedEntity<V>> queryOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException {
        try {
            String entityOnePath = this.host.concat("/").concat(entity.entityName).concat("/one");
            logger.info("Consultando ruta {}", entityOnePath);

            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder.uri(new URI(entityOnePath));
            builder.POST(HttpRequest.BodyPublishers.ofString(KSAProjector.toJSON(id), StandardCharsets.UTF_8));
            HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            logger.info("HttpResponse {} {}", response.statusCode(), response.body());

            if (response.statusCode() == 404) {
                logger.info("No encontrado");
                return Optional.empty();
            }

            JavaType type = TypeFactory.defaultInstance().constructParametricType(MaterializedEntity.class, TypeFactory.defaultInstance().constructType(entity.valueType));

            MaterializedEntity<V> element = KSAProjector.fromJSON(response.body(), type);

            logger.info("Respuesta {}", element.toString());

            return Optional.of(element);

        } catch (Exception e) {
            logger.error("Error consulta", e);
            throw new CommandProjectorQueryException(e.toString());
        }
    }

    @Override
    public <K, V extends EntityValue<V>> List<Event> traceOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException {
        return List.of();
    }

    static private <T> T fromJSON(String json, JavaType typeReference) {
        ObjectMapper objectMapper =
                new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static private String toJSON(Object o) {
        ObjectMapper objectMapper =
                new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
