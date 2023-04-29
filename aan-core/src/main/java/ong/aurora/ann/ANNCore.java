package ong.aurora.ann;

import com.google.common.net.HostAndPort;
import ong.aurora.ann.command.CommandPool;
import ong.aurora.ann.command.CommandRestService;
import ong.aurora.ann.network.AANNetwork;
import ong.aurora.ann.network.AANNetworkHost;
import ong.aurora.ann.network.AANNetworkNode;
import ong.aurora.ann.network.AANNetworkNodeStatusType;
import ong.aurora.ann.network.libp2p.libp2pNetwork;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.config.AANConfig;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.peer.node.AANNodeEntity;
import ong.aurora.commons.peer.node.AANNodeStatus;
import ong.aurora.commons.peer.node.AANNodeValue;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.commons.projector.rdb_projector.RDBProjector;
import ong.aurora.commons.serialization.AANSerializer;
import ong.aurora.commons.serialization.jackson.AANJacksonSerializer;
import ong.aurora.commons.store.file.FileEventStore;
import ong.aurora.model.v_0_0_1.AuroraOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ANNCore {

    private static final Logger log = LoggerFactory.getLogger(ANNCore.class);

    public static void main(String[] args) throws Exception {


        AANSerializer aanSerializer = new AANJacksonSerializer();

        AANModel aanModel = new AuroraOM();

        AANConfig aanConfig = AANConfig.fromEnviroment();

        log.info("Iniciando nodo {}", aanConfig.nodeId);

        AANBlockchain aanBlockchain = new AANBlockchain(new FileEventStore(aanConfig.blockchainFilePath), aanSerializer);

        if (aanBlockchain.isEmpty()) {
            log.info("!! Blockchain no inicializada");


        } else {
            log.info("!! Blockchain encontrada ({} bloques)", aanBlockchain.blockCount());
            log.info("Verificando integridad");
            aanBlockchain.verifyIntegrity().get();
        }

        AANProjector aanProjector = new RDBProjector(aanSerializer, aanConfig, aanModel);

        aanProjector.startProjector().get();

        log.info("Projector iniciado {}", aanProjector);

        AANProcessor aanProcessor = new AANProcessor(aanBlockchain, aanModel, aanProjector);

        long eventCount2 = aanBlockchain.blockCount();
        log.info("Cargando {} eventos", eventCount2);

        aanBlockchain.eventStream().forEachOrdered(event -> {
            log.debug("Leyendo eventStore {}", event);
            try {
                aanProjector.projectEvent(event).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });

        // CUANDO SE ESCRIBE UN EVENTO EN LA BLOCKCHAIN, PROYECTARLO INMEDIATAMNETE
        aanBlockchain.lastEventStream.asObservable().skip(1).subscribe(event -> {
            log.info("Proyectando evento {}", event);
            try {
                aanProjector.projectEvent(event).join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // COMMAND POOL REST SERVICE

        CommandPool commandPool = new CommandPool();

        CommandRestService commandRestService = new CommandRestService(HostAndPort.fromParts("127.0.0.1", aanConfig.commandPort), aanProcessor, commandPool);
        commandRestService.start();

        // AAN NETWORK

        AANNetwork aanNetwork = new libp2pNetwork(aanConfig, aanSerializer, aanBlockchain);
        aanNetwork.startHost().join();

        BehaviorSubject<List<AANNetworkNode>> networkNodes = BehaviorSubject.create();
        BehaviorSubject<List<AANNodeValue>> projectorNodes = BehaviorSubject.create();

        Scheduler nodeUpdateScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

        // PROJECTOR NODES UPDATE
        aanBlockchain.lastEventStream
                .observeOn(nodeUpdateScheduler)
                .filter(event -> Objects.equals(Optional.ofNullable(event).map(Event::eventName).orElse(""), "aan_node"))
                .throttleLast(1, TimeUnit.SECONDS, nodeUpdateScheduler)
                .doOnSubscribe(() -> {
                    try {
                        List<MaterializedEntity<AANNodeValue>> allNodeList = aanProjector.queryAll(new AANNodeEntity());
                        projectorNodes.onNext(allNodeList.stream().map(MaterializedEntity::getEntityValue).toList());
                    } catch (CommandProjectorQueryException e) {
                        // TODO FATAL
                        throw new RuntimeException(e);
                    }
                })
                .subscribe(event -> {
                    try {
                        List<MaterializedEntity<AANNodeValue>> allNodeList = aanProjector.queryAll(new AANNodeEntity());
                        log.info("Nodos actualizados desde proyector {}", allNodeList);
                        projectorNodes.onNext(allNodeList.stream().map(MaterializedEntity::getEntityValue).toList());
                    } catch (CommandProjectorQueryException e) {
                        throw new RuntimeException(e);
                    }


                });

        projectorNodes.asObservable()
                .doOnEach(notification -> log.info("!! Projector nodes"))
                .observeOn(nodeUpdateScheduler)
                .subscribe(annNodeValues -> {

                    log.info("======= projectorNodes actualizados =======");
                    annNodeValues.forEach(annNodeValue -> log.info(annNodeValue.toString()));
                    log.info("==============");

                    // CERRAR CONEXIÓN
                    if (networkNodes.hasValue()) {
                        networkNodes.getValue().stream().filter(node -> node.currentStatus() != AANNetworkNodeStatusType.DISCONNECTED).forEach(AANNetworkNode::terminateNodeConnection);
                    }

                    // CREAR NUEVOS NETWORK PEERS
                    List<AANNetworkNode> networkNodes1 = annNodeValues.stream().filter(aanNodeValue -> aanNodeValue.nodeStatus() == AANNodeStatus.ACTIVE).filter(aanNodeValue -> !aanNodeValue.nodeId().equals(aanConfig.nodeId)).map(nodeValue -> new AANNetworkNode(nodeValue, aanBlockchain)).toList();
                    networkNodes.onNext(networkNodes1);

                });


        aanNetwork.onNetworkConnection()
                .observeOn(Schedulers.io())
                .subscribe(incomingPeer -> {

                    log.info("Nueva conexión entrante {} ", incomingPeer.getPeerIdentity());

                    if (networkNodes.getValue().isEmpty()) {
                        // CREAR NETWORK PEER Y PUSHEAR
                        AANNetworkNode networkNode = new AANNetworkNode(new AANNodeValue("unknown", "Desconocido", "", "", "", AANNodeStatus.ACTIVE), aanBlockchain);
                        networkNode.attachConnection(incomingPeer);
                        networkNodes.onNext(List.of(networkNode));
                    } else {
                        Optional<AANNetworkNode> networkNodeOptional = networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.aanNodeValue.nodeSignature().equals(incomingPeer.getPeerIdentity())).findAny();

                        if (networkNodeOptional.isPresent()) {
                            AANNetworkNode networkNode = networkNodeOptional.get();
                            networkNode.attachConnection(incomingPeer);
                        } else {
                            log.info("No se pudo encontrar ningún networkNode para {}, cerrando conexión", incomingPeer.getPeerIdentity());
                            incomingPeer.closeConnection();
                        }
                    }

                });


        networkNodes.asObservable()
                .doOnEach(notification -> log.info("!! Network nodes"))
                .observeOn(nodeUpdateScheduler)
                .subscribe(annNodeValues -> {
                    log.info("======= networkNodes actualizados =======");
                    annNodeValues.forEach(annNodeValue -> log.info(annNodeValue.toString()));
                    log.info("==============");
                });

        new AANNetworkHost(networkNodes, aanNetwork, aanBlockchain, nodeUpdateScheduler);

    }
}
