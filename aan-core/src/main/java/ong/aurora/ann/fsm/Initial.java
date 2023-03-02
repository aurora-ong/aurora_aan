package ong.aurora.ann.fsm;

import com.google.common.net.HostAndPort;
import ong.aurora.ann.AANConfig;
import ong.aurora.ann.AANProcessor;
import ong.aurora.ann.command.CommandPool;
import ong.aurora.ann.command.CommandRestService;
import ong.aurora.ann.p2p_2.AANNetwork;
import ong.aurora.ann.p2p_2.libp2pHostNode;
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
import org.springframework.statemachine.annotation.OnStateEntry;
import org.springframework.statemachine.annotation.WithStateMachine;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@WithStateMachine
public class Initial {

    @Autowired
    private StateMachine<AANState, AANEvent> stateMachine;

    private static final Logger log = LoggerFactory.getLogger(Initial.class);

    //    @OnTransition(source = "INICIAL", target = "CONFIG_LOADING")
    @OnStateEntry(target = "CONFIG_LOADING")
    public void onConfigLoading(
            @EventHeader("aanSerializer") AANSerializer aanSerializer,
            @EventHeader("aanModel") AANModel aanModel
    ) {
        log.info("Iniciando AAN Node");

        AANConfig aanConfig = AANConfig.fromEnviroment();

        Message<AANEvent> message = MessageBuilder
                .withPayload(AANEvent.CONFIG_OK)
                .setHeader("aanConfig", aanConfig)
                .setHeader("aanModel", aanModel)
                .setHeader("aanSerializer", aanSerializer)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();


    }

    @OnStateEntry(source = "CONFIG_LOADING", target = "CONFIG_START")
    public void onConfigStart() throws Exception {
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
    public void onBlockchainLoading(
            @EventHeader("aanSerializer") AANSerializer aanSerializer,
            @EventHeader("aanConfig") AANConfig aanConfig,
            @EventHeader("aanModel") AANModel aanModel
    ) throws Exception {
        ANNEventStore eventStore = new FileEventStore(aanConfig.getBlockchainFilePath());
        AANBlockchain aanBlockchain = new AANBlockchain(eventStore, aanSerializer);

        log.info("Blockchain inicializada ({} bloques)", aanBlockchain.blockCount());
        aanBlockchain.verifyIntegrity().get();


        Message<AANEvent> message = MessageBuilder
                .withPayload(AANEvent.BLOCKCHAIN_OK)
                .setHeader("aanConfig", aanConfig)
                .setHeader("aanModel", aanModel)
                .setHeader("aanSerializer", aanSerializer)
                .setHeader("aanBlockchain", aanBlockchain)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();
    }

    @OnStateEntry(source = "BLOCKCHAIN_LOADING", target = "PROJECTOR_LOADING")
    public void onProjectorLoading(
            @EventHeader("aanModel") AANModel aanModel,
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
                .withPayload(AANEvent.PROJECTOR_OK)
                .setHeader("aanConfig", aanConfig)
                .setHeader("aanSerializer", aanSerializer)
                .setHeader("aanBlockchain", aanBlockchain)
                .setHeader("aanModel", aanModel)
                .setHeader("aanProjector", aanProjector)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();
    }

    @OnStateEntry(source = "PROJECTOR_LOADING", target = "PROCESSOR_LOADING")
    public void onProcessorLoading(
            @EventHeader("aanModel") AANModel aanModel,
            @EventHeader("aanConfig") AANConfig aanConfig,
            @EventHeader("aanBlockchain") AANBlockchain aanBlockchain,
            @EventHeader("aanSerializer") AANSerializer aanSerializer,
            @EventHeader("aanProjector") AANProjector aanProjector) throws Exception {
        log.info("Inicializando procesador");

        AANProcessor aanProcessor = new AANProcessor(aanBlockchain, aanModel, aanProjector);

        Message<AANEvent> message = MessageBuilder
                .withPayload(AANEvent.PROCESSOR_OK)
                .setHeader("aanConfig", aanConfig)
                .setHeader("aanSerializer", aanSerializer)
                .setHeader("aanModel", aanModel)
                .setHeader("aanBlockchain", aanBlockchain)
                .setHeader("aanProjector", aanProjector)
                .setHeader("aanProcessor", aanProcessor)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();

    }

    @OnStateEntry(source = "PROCESSOR_LOADING", target = "NODE_LOADING")
    public void onNodeLoading(
            @EventHeader("aanModel") AANModel aanModel,
            @EventHeader("aanConfig") AANConfig aanConfig,
            @EventHeader("aanBlockchain") AANBlockchain aanBlockchain,
            @EventHeader("aanSerializer") AANSerializer aanSerializer,
            @EventHeader("aanProjector") AANProjector aanProjector,
            @EventHeader("aanProcessor") AANProcessor aanProcessor

    ) throws Exception {
        log.info("Inicializando nodo {}", aanConfig.getNodeId());

        // INICIAR REST COMMAND
        CommandPool commandPool = new CommandPool();

        Integer commandApiPort = aanConfig.getCommandRestPort();

        CommandRestService commandRestService = new CommandRestService(HostAndPort.fromParts("127.0.0.1", commandApiPort), aanProcessor, commandPool);
        commandRestService.start();


        // P2P

        AANNetwork hostNode = new libp2pHostNode(aanConfig, aanSerializer, aanBlockchain);
        hostNode.startHost();
    }

//    @OnStateEntry(source = "NODE_LOADING", target = "NODE_READY")
//    public void onNodeReady(
//            @EventHeader("aanModel") AANModel aanModel,
//            @EventHeader("aanConfig") AANConfig aanConfig,
//            @EventHeader("aanBlockchain") AANBlockchain aanBlockchain,
//            @EventHeader("aanSerializer") AANSerializer aanSerializer,
//            @EventHeader("aanProjector") AANProjector aanProjector,
//            @EventHeader("aanProcessor") AANProcessor aanProcessor
//
//    ) throws Exception {
//        log.info("Inicializando nodo {}", aanConfig.getNodeId());
//
//        // INICIAR REST COMMAND
//        CommandPool commandPool = new CommandPool();
//
//        Integer commandApiPort = aanConfig.getCommandRestPort();
//
//        CommandRestService commandRestService = new CommandRestService(HostAndPort.fromParts("127.0.0.1", commandApiPort), aanProcessor, commandPool);
//        commandRestService.start();
//
//
//        // P2P
//
//        Ip2pHostNode hostNode = new libp2pHostNode(aanConfig, aanSerializer, aanBlockchain);
//    }



}
