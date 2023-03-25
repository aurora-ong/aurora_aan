package ong.aurora.ann;

import com.google.common.net.HostAndPort;
import ong.aurora.ann.command.CommandPool;
import ong.aurora.ann.command.CommandRestService;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.config.AANConfig;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.peer.node.ANNNodeEntity;
import ong.aurora.commons.peer.node.ANNNodeValue;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.commons.projector.rdb_projector.RDBProjector;
import ong.aurora.commons.serialization.AANSerializer;
import ong.aurora.commons.serialization.jackson.AANJacksonSerializer;
import ong.aurora.commons.store.file.FileEventStore;
import ong.aurora.model.v_0_0_1.AuroraOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        // OBTENER NODOS ACTIVOS

        List<MaterializedEntity<ANNNodeValue>> allNodeList = aanProjector.queryAll(new ANNNodeEntity());
        log.info("Nodos obtenidos {}", allNodeList);

        allNodeList.forEach(annNodeValueMaterializedEntity -> {
            log.info("Nodo {}", annNodeValueMaterializedEntity.getEntityValue());
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

//
//        AANNetwork hostNode = new libp2pNetwork(aanConfig, aanSerializer, aanBlockchain);
//        hostNode.startHost();
//
//        BehaviorSubject<List<AANNetworkNode>> networkNodes = BehaviorSubject.create(List.of());
//        BehaviorSubject<List<ANNNodeValue>> projectorNodes = BehaviorSubject.create(List.of());
//
//
//        projectorNodes.asObservable().subscribe(annNodeValues -> {
//
//            log.info("========= \nProjector nodes actualizado: ");
//            annNodeValues.forEach(annNodeValue -> log.info(annNodeValue.toString()));
//            log.info("========= \n");
//
//            // CERRAR CONEXIÓN
//            networkNodes.getValue().forEach(o -> {});
//
//            // CREAR NETWORK PEERS
//            networkNodes.onNext(List.of());
//        });
//
//
//
//        hostNode.onNetworkConnection().subscribe(networkPeer -> {
//
//            log.info("Nueva conexión {} ", networkPeer.toString());
//
//            if (networkNodes.getValue().isEmpty()) {
//                // CREAR NETWORK PEER Y PUSHEAR
//                networkNodes.onNext(List.of());
//            }
//
//
//            if (!networkNodes.getValue().isEmpty()) {
//                // CHECKEAR QUE EXISTA EN NETWORKNODES, SI ESTÁ ASIGNAR, ELSE THROW
//            }
//
//
//        });


//        log.info("Actualizando nodos");
//        projectorNodes.onNext(allNodeList.stream().map(MaterializedEntity::getEntityValue).toList());


    }
}
