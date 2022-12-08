package ong.aurora.processor;

import ong.aurora.commons.command.Command;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.store.file.FileEventStore;
import ong.aurora.commons.serialization.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;


class ProcessorKSA {

    private static final Logger log = LoggerFactory.getLogger(ProcessorKSA.class);

    public static void main(String[] args) throws IOException {

        // ANN NODE
        FileEventStore eventStore = new FileEventStore("events.ann");

        log.info("Cargando processor-ksa");
        Topology builder = new Topology();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "processor-ksa");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
//        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);

        // TOPIC COMANDOS
        builder.addSource(
                "Aurora AAN Commands", // name
                Serdes.String().deserializer(), // deserializador key
                JsonSerdes.getJSONSerde(Command.class).deserializer(), // deserializador value
                "aurora-aan-commands"); // topic

        // PROCESADOR
        builder.addProcessor(
                "Aurora AAN Command Validator", // name
                () -> new AANProcessor(eventStore), // clase que procesa
                "Aurora AAN Commands"); // parent

        // TOPIC EVENTOS
        builder.addSink(
                "Aurora AAN Events", // name
                "aurora-aan-events", // topic
                Serdes.Long().serializer(), // serializador key
                JsonSerdes.getJSONSerde(Event.class).serializer(), // serializador value
                "Aurora AAN Command Validator"); // parent


        KafkaStreams streams = new KafkaStreams(builder, props);

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.cleanUp();
        log.info("Iniciando processor-ksa");
        streams.start();

    }
}