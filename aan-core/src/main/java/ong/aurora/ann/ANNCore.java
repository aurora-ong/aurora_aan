package ong.aurora.ann;

import com.google.common.net.HostAndPort;
import ong.aurora.ann.command.CommandPool;
import ong.aurora.ann.command.CommandRestService;
import ong.aurora.ann.identity.ANNNodeIdentity;
import ong.aurora.ann.p2p.p2pHostNode;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.peer.node.ANNNodeEntity;
import ong.aurora.commons.peer.node.ANNNodeValue;
import ong.aurora.commons.projector.ksaprojector.KSAProjector;
import ong.aurora.commons.serialization.AANSerializer;
import ong.aurora.commons.serialization.jackson.AANJacksonSerializer;
import ong.aurora.commons.store.file.FileEventStore;
import ong.aurora.model.v_0_0_1.AuroraOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.BehaviorSubject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ANNCore {

    private static final Logger log = LoggerFactory.getLogger(ANNCore.class);

    public static void main(String[] args) throws Exception, CommandProjectorQueryException {


        AANSerializer annSerializer = new AANJacksonSerializer();

        AANModel aanModel = new AuroraOM();

        Map<String, String> env = System.getenv();

        String nodeId = env.get("ANN_NODE_ID");

        if (nodeId == null || nodeId.isEmpty()) {
            throw new Exception("Debe proporcionarse un identificador de nodo");
        }

        log.info("Iniciando nodo {}", nodeId);

        String nodeInfoPath = env.get("ANN_NODE_INFO_PATH");

        ANNNodeIdentity nodeIdentity = ANNNodeIdentity.fromFile(nodeInfoPath.concat(nodeId).concat("/identity_private.pem"), nodeInfoPath.concat(nodeId).concat("/identity_public.pem"));

        AANBlockchain blockchain = new AANBlockchain(new FileEventStore(nodeInfoPath.concat(nodeId).concat("/event_store.log")), annSerializer);

        if (blockchain.isEmpty()) {
            log.info("!! Blockchain no inicializada");


        } else {
            log.info("!! Blockchain encontrada ({} bloques)", blockchain.blockCount());
            log.info("Verificando integridad");
            blockchain.verifyIntegrity().get();
        }

        KSAProjector aanProjector = new KSAProjector("http://localhost:15002", "localhost:29092");

//        aanProjector.startProjector(aanModel).get();

        log.info("Projector iniciado {}", aanProjector.toString());

        AANProcessor aanProcessor = new AANProcessor(blockchain, aanModel, aanProjector);

        long eventCount2 = blockchain.blockCount();
        log.info("Cargando {} eventos", eventCount2);

        blockchain.eventStream().forEachOrdered(event -> {
            log.info("Leyendo eventStore {}", event);
            try {
                aanProjector.projectEvent(event).get();
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

        Optional<MaterializedEntity<ANNNodeValue>> thisNodeOptional = allNodeList.stream().filter(annNodeValueMaterializedEntity -> annNodeValueMaterializedEntity.getEntityValue().nodeId().equals(nodeId)).findFirst();

        log.info("Nodos optional {}", thisNodeOptional);

        if (thisNodeOptional.isEmpty()) {
            throw new Exception("Nodo no registrado en blockchain");
        }

        ANNNodeValue thisNode = thisNodeOptional.get().getEntityValue();

        log.info("This node {}", thisNode);

        if (!nodeIdentity.compareWith(thisNode.nodeSignature())) {
            throw new Exception("Las firmas no coinciden");
        }

        // INICIAR REST COMMAND

        CommandPool commandPool = new CommandPool();

        String commandApiPort = env.get("ANN_NODE_COMMAND_API_PORT");


        CommandRestService commandRestService = new CommandRestService(HostAndPort.fromParts("127.0.0.1", Integer.parseInt(commandApiPort)), aanProcessor, commandPool);
        commandRestService.start();

        BehaviorSubject<List<ANNNodeValue>> projectorNodes = BehaviorSubject.create(List.of());

        p2pHostNode p2PNode = new p2pHostNode(nodeIdentity, thisNode, annSerializer, projectorNodes, blockchain);

//        p2PNode.start().get();

        log.info("Actualizando nodos");
        projectorNodes.onNext(allNodeList.stream().map(MaterializedEntity::getEntityValue).toList());


    }


}
