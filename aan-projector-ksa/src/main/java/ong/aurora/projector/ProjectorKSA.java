package ong.aurora.projector;

import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.peer.node.ANNNodeEntity;
import ong.aurora.commons.serialization.JsonSerdes;
import ong.aurora.model.v_0_0_1.AuroraOM;
import ong.aurora.model.v_0_0_1.entity.person.PersonEntity;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.HostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class ProjectorKSA {

    private static final Logger log = LoggerFactory.getLogger(ProjectorKSA.class);

    private static AANModel aanModel = new AuroraOM();

    public static void main(String[] args) {
        log.info("Cargando projector-ksa");

        List<AANEntity> aanEntities = List.of(new ANNNodeEntity());

        List<AANEntity> proyectorEntities = new ArrayList<>();
        proyectorEntities.addAll(aanEntities);
        proyectorEntities.addAll(aanModel.getModelEntities());


        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "projector-ksa");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
//        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        StreamsBuilder builder = new StreamsBuilder();
//
        KStream<Long, Event> eventKStream =
                builder.stream(
                        "aurora-aan-events",
                        Consumed.with(Serdes.Long(), JsonSerdes.getJSONSerde(Event.class)));

        eventKStream.print(Printed.<Long, Event>toSysOut().withLabel("aurora-aan-events"));

        BranchedKStream<Long, Event> branchedEvents2 =
                eventKStream.split(Named.as("projector-"));

        proyectorEntities.forEach(entity -> {
            Predicate<Long, Event> createPredicate =
                    (key, event) -> event.eventName().equals(entity.entityName.concat(".created"));
            branchedEvents2.branch(createPredicate, Branched.as(entity.entityName));
        });

        Map<String, KStream<Long, Event>> branchedEvents = branchedEvents2.noDefaultBranch();
        proyectorEntities.forEach(entity -> EntityProjector.configureBranch(entity, branchedEvents));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.cleanUp();
        log.info("Iniciando projector-ksa");
        streams.start();

        HostInfo hostInfo = new HostInfo("localhost", 15002);
//        RestService service = new RestService(hostInfo, streams);
        RestService service = new RestService(hostInfo, streams, proyectorEntities);
        log.info("Starting Digital Twin REST Service");
        service.start();

    }
}