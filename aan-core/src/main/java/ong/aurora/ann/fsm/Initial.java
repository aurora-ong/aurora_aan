package ong.aurora.ann.fsm;

import ong.aurora.ann.AANConfig;
import ong.aurora.ann.AANProcessor;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.commons.projector.ksaprojector.KSAProjector;
import ong.aurora.commons.serialization.AANSerializer;
import ong.aurora.commons.store.ANNEventStore;
import ong.aurora.commons.store.file.FileEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.EventHeader;
import org.springframework.statemachine.annotation.EventHeaders;
import org.springframework.statemachine.annotation.OnStateEntry;
import org.springframework.statemachine.annotation.WithStateMachine;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@WithStateMachine
public class Initial {

    @Autowired
    private StateMachine<AANState, AANEvent> stateMachine;

    private static final Logger log = LoggerFactory.getLogger(Initial.class);

    //    @OnTransition(source = "INICIAL", target = "CONFIG_LOADING")
    @OnStateEntry(target = "CONFIG_LOADING")
    public void fromS1ToS22(@EventHeader("aanSerializer") AANSerializer aanSerializer) {
        log.info("Iniciando AAN Node");

        AANConfig aanConfig = AANConfig.fromEnviroment();

        Message<AANEvent> message = MessageBuilder
                .withPayload(AANEvent.CONFIG_OK)
                .setHeader("aanConfig", aanConfig)
                .setHeader("aanSerializer", aanSerializer)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();


    }

    @OnStateEntry(source = "CONFIG_LOADING", target = "CONFIG_START")
    public void startBlockchain() throws Exception {
        log.info("Introduce el identificador del nodo: ");
        String name;
        do {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            // Reading data using readLine
            name = reader.readLine();

            // Printing the read line
            log.info("Echo: {}", name);
        } while (!name.equals("exit"));


    }

    @OnStateEntry(source = "CONFIG_LOADING", target = "BLOCKCHAIN_LOADING")
    public void onBlockchainLoading(@EventHeader("aanSerializer") AANSerializer aanSerializer, @EventHeader("aanConfig") AANConfig aanConfig, @EventHeaders Map<String, Object> headers) throws Exception {
        ANNEventStore eventStore = new FileEventStore(aanConfig.getBlockchainFilePath());
        AANBlockchain aanBlockchain = new AANBlockchain(eventStore, aanSerializer);

        log.info("Blockchain inicializada ({} bloques)", aanBlockchain.blockCount());
        aanBlockchain.verifyIntegrity().get();


        Message<AANEvent> message = MessageBuilder
                .withPayload(AANEvent.BLOCKCHAIN_OK)
                .setHeader("aanConfig", aanConfig)
                .setHeader("aanSerializer", aanSerializer)
                .setHeader("aanBlockchain", aanBlockchain)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();
    }

    @OnStateEntry(source = "BLOCKCHAIN_LOADING", target = "PROJECTOR_LOADING")
    public void onProjectorLoading(
            @EventHeader("aanBlockchain") AANBlockchain aanBlockchain,
            @EventHeader("aanSerializer") AANSerializer aanSerializer,
            @EventHeader("aanConfig") AANConfig aanConfig) {
        AANProjector aanProjector = new KSAProjector("http://localhost:15002", "localhost:29092");

//        aanProjector.startProjector(aanModel).get();

        log.info("Inicializando proyector con {} eventos", aanBlockchain.blockCount());

        aanBlockchain.eventStream().forEachOrdered(event -> {
            log.info("Leyendo eventStore {}", event);
            try {
                aanProjector.projectEvent(event).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Message<AANEvent> message = MessageBuilder
                .withPayload(AANEvent.BLOCKCHAIN_OK)
                .setHeader("aanConfig", aanConfig)
                .setHeader("aanSerializer", aanSerializer)
                .setHeader("aanBlockchain", aanBlockchain)
                .setHeader("aanProjector", aanProjector)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();
    }

    @OnStateEntry(source = "PROJECTOR_LOADING", target = "PROCESSOR_LOADING")
    public void onProcessorLoading(
            @EventHeader("aanModel") AANModel aanModel,
            @EventHeader("aanBlockchain") AANBlockchain aanBlockchain,
            @EventHeader("aanSerializer") AANSerializer aanSerializer,
            @EventHeader("aanProjector") AANProjector aanProjector) throws Exception {
        AANProcessor aanProcessor = new AANProcessor(aanBlockchain, aanModel, aanProjector);

    }

    @OnStateEntry(source = "PROCESSOR_LOADING", target = "NODE_LOADING")
    public void onNodeLoading(
            @EventHeader("aanModel") AANModel aanModel,
            @EventHeader("aanBlockchain") AANBlockchain aanBlockchain,
            @EventHeader("aanSerializer") AANSerializer aanSerializer,
            @EventHeader("aanProjector") AANProjector aanProjector) throws Exception {
        AANProcessor aanProcessor = new AANProcessor(aanBlockchain, aanModel, aanProjector);

    }

}
