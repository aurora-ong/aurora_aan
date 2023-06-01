package ong.aurora.aan.core;

import com.google.common.net.HostAndPort;
import ong.aurora.aan.blockchain.AANBlockchain;
import ong.aurora.aan.command.CommandProjectorQueryException;
import ong.aurora.aan.config.AANConfig;
import ong.aurora.aan.core.command_pool.CommandPool;
import ong.aurora.aan.core.command_pool.CommandRestService;
import ong.aurora.aan.core.network.AANNetwork;
import ong.aurora.aan.core.network.AANNetworkHost;
import ong.aurora.aan.core.network.AANNetworkNode;
import ong.aurora.aan.core.network.AANNetworkNodeStatusType;
import ong.aurora.aan.core.network.libp2p.libp2pNetwork;
import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.event.Event;
import ong.aurora.aan.model.AANModel;
import ong.aurora.aan.model.v_0_0_1.AuroraOM;
import ong.aurora.aan.node.AANNodeEntity;
import ong.aurora.aan.node.AANNodeStatus;
import ong.aurora.aan.node.AANNodeValue;
import ong.aurora.aan.projector.AANProjector;
import ong.aurora.aan.projector.rdb_projector.RDBProjector;
import ong.aurora.aan.serialization.AANSerializer;
import ong.aurora.aan.serialization.jackson.AANJacksonSerializer;
import ong.aurora.aan.store.file.FileEventStore;
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
                .observeOn(nodeUpdateScheduler)
                .subscribe(incomingPeer -> {

                    log.info("Nueva conexión entrante {} ", incomingPeer.getPeerIdentity());

                    if (networkNodes.getValue().isEmpty()) {
                        // CREAR NETWORK PEER ANÓNIMO
                        AANNetworkNode networkNode = new AANNetworkNode(aanBlockchain);
                        networkNode.attachConnection(incomingPeer);
                        networkNodes.onNext(List.of(networkNode));
                    } else {
                        Optional<AANNetworkNode> networkNodeOptional = networkNodes.getValue().stream().filter(aanNetworkNode -> !aanNetworkNode.isAnonymous()).filter(aanNetworkNode -> aanNetworkNode.aanNodeValue.nodeSignature().equals(incomingPeer.getPeerIdentity())).findAny();

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

        AANNetworkHost aanHost = new AANNetworkHost(networkNodes, aanNetwork, aanBlockchain, nodeUpdateScheduler, aanProcessor);

        // COMMAND POOL REST SERVICE

        CommandRestService commandRestService = new CommandRestService(HostAndPort.fromParts("127.0.0.1", aanConfig.commandPort), aanHost);
        commandRestService.start();


    }
}
