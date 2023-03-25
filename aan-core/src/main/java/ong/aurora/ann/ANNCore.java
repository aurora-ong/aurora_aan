package ong.aurora.ann;

import com.google.common.net.HostAndPort;
import ong.aurora.ann.command.CommandPool;
import ong.aurora.ann.command.CommandRestService;
import ong.aurora.ann.network.AANNetwork;
import ong.aurora.ann.network.AANNetworkNode;
import ong.aurora.ann.network.AANNetworkNodeStatusType;
import ong.aurora.ann.network.AANNetworkPeer;
import ong.aurora.ann.network.libp2p.libp2pNetwork;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.config.AANConfig;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.peer.node.ANNNodeEntity;
import ong.aurora.commons.peer.node.AANNodeValue;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.commons.projector.rdb_projector.RDBProjector;
import ong.aurora.commons.serialization.AANSerializer;
import ong.aurora.commons.serialization.jackson.AANJacksonSerializer;
import ong.aurora.commons.store.file.FileEventStore;
import ong.aurora.model.v_0_0_1.AuroraOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ANNCore {

    private static final Logger log = LoggerFactory.getLogger(ANNCore.class);

    public static void main(String[] args) throws Exception, CommandProjectorQueryException {


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

        // CUANDO SE ESCRIBE UN EVENTO EN LA BLICKCHAIN, PROYECTARLO INMEDIATAMNETE
        aanBlockchain.onEventPersisted.asObservable().subscribe(event -> {
            log.info("Proyectando evento {}", event);
            try {
                aanProjector.projectEvent(event).join();

                // ACTUALIZAR NODOS

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


//        Optional<MaterializedEntity<ANNNodeValue>> thisNodeOptional = allNodeList.stream().filter(annNodeValueMaterializedEntity -> annNodeValueMaterializedEntity.getEntityValue().nodeId().equals(aanConfig.getNodeId())).findFirst();
//
//        log.info("Nodos optional {}", thisNodeOptional);
//
//        if (thisNodeOptional.isEmpty()) {
//            throw new Exception("Nodo no registrado en blockchain");
//        }
//
//        ANNNodeValue thisNode = thisNodeOptional.get().getEntityValue();
//
//        log.info("This node {}", thisNode);
//
////        if (!nodeIdentity.compareWith(thisNode.nodeSignature())) {
////            throw new Exception("Las firmas no coinciden");
////        }
////
//
        // INICIAR REST COMMAND
        CommandPool commandPool = new CommandPool();

        CommandRestService commandRestService = new CommandRestService(HostAndPort.fromParts("127.0.0.1", aanConfig.commandPort), aanProcessor, commandPool);
        commandRestService.start();

        AANNetwork aanNetwork = new libp2pNetwork(aanConfig, aanSerializer, aanBlockchain);
        aanNetwork.startHost().join();

        BehaviorSubject<List<AANNetworkNode>> networkNodes = BehaviorSubject.create(List.of());
        BehaviorSubject<List<AANNodeValue>> projectorNodes = BehaviorSubject.create(List.of());

        // OBTENER NODOS ACTIVOS (INICIAL)

        List<MaterializedEntity<AANNodeValue>> allNodeList = aanProjector.queryAll(new ANNNodeEntity());
        log.info("Nodos obtenidos {}", allNodeList);

        projectorNodes.onNext(allNodeList.stream().map(MaterializedEntity::getEntityValue).toList());

//        allNodeList.forEach(annNodeValueMaterializedEntity -> {
//            log.info("Nodo {}", annNodeValueMaterializedEntity.getEntityValue());
//        });

        projectorNodes.asObservable().subscribe(annNodeValues -> {

            log.info("\n======= projectorNodes actualizados =======");
            annNodeValues.forEach(annNodeValue -> log.info(annNodeValue.toString()));
            log.info("\n==============");

            // CERRAR CONEXIÓN
            networkNodes.getValue().forEach(o -> {
            });

            // CREAR NETWORK PEERS
            List<AANNetworkNode> networkNodes1 = annNodeValues.stream().filter(aanNodeValue -> !aanNodeValue.nodeId().equals(aanConfig.nodeId)).map(nodeValue -> new AANNetworkNode(nodeValue, null)).toList();
//            networkNodes1.forEach(aanNetwork::establishConnection);

            networkNodes.onNext(networkNodes1);
        });


        aanNetwork.onNetworkConnection().subscribe(incomingPeer -> {

            log.info("Nueva conexión {} ", incomingPeer.toString());

            if (networkNodes.getValue().isEmpty()) {
                // CREAR NETWORK PEER Y PUSHEAR
                AANNetworkNode networkNode = new AANNetworkNode(null, incomingPeer);
                networkNodes.onNext(List.of(networkNode));
            }


            if (!networkNodes.getValue().isEmpty()) {

                Optional<AANNetworkNode> networkNodeOptional = networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.aanNodeValue.nodeSignature().equals(incomingPeer.getPeerIdentity())).findAny();

                if (networkNodeOptional.isPresent()) {
                    AANNetworkNode networkNode = networkNodeOptional.get();
                    networkNode.attachConnection(incomingPeer);
                } else {
                    log.info("No se pudo encontrar ningún networkNode para {}, ignorando", incomingPeer.getPeerIdentity());
                }
            }


        });


        networkNodes.asObservable().subscribe(annNodeValues -> {

            log.info("\n======= networkNodes actualizados =======");
            annNodeValues.forEach(annNodeValue -> {
                log.info(annNodeValue.toString());
                aanNetwork.establishConnection(annNodeValue);
            });
            log.info("\n==============");

//            aanNetwork.startHost();

        });

        networkNodes.asObservable().flatMap(aanNetworkNodes -> {
            return Observable.combineLatest(aanNetworkNodes.stream().map(AANNetworkNode::onStatusChange).toList(), args1 -> {
                List<AANNetworkNode> peerStatusList = (List<AANNetworkNode>) (List<?>) Arrays.stream(args1).toList();
                return peerStatusList;
            });
        }, (aanNetworkNodes, o) -> {

            return aanNetworkNodes;

        }).subscribe(o -> {
            log.info("\n======= Red actualizada =======");
            o.forEach(aanNetworkNode -> log.info(aanNetworkNode.toString()));
            log.info("==============");
        });

    }
}
