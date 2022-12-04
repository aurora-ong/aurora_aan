package ong.aurora.ann;

import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.eventsource.FileEventSource;
import ong.aurora.commons.peer.node.ANNNodeEntity;
import ong.aurora.commons.peer.node.ANNNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ANNCore {

    private static final Logger log = LoggerFactory.getLogger(ANNCore.class);

    public static void main(String[] args) throws Exception {

        Map<String, String> env = System.getenv();

        String nodeId = env.get("ANN_NODE_ID");

        if (nodeId == null || nodeId.isEmpty()) {
            throw new Exception("Debe proporcionarse un identificador de nodo");
        }



        log.info("Iniciando nodo {}", nodeId);

//        FileEventSource eventSource = new FileEventSource("events.ann");

        // TODO CHECKEAR ESTADO BLOCKCHAIN
        // TODO OBTENER NODOS ACTIVOS


        ANNNodeIdentity nodeIdentity;
        try {
            nodeIdentity = new ANNNodeIdentity(nodeId);
        } catch (Exception e) {
            log.error("No se pudo obtener la identidad del nodo", e);
            e.printStackTrace();
        }

        // TODO COMPROBAR IDENTIDAD CON NODO MATERIALIZADO


//        List<MaterializedEntity<ANNNodeValue>> nodeEntityList = List.of(
//                new ANNNodeValue(
//                        "node_1",
//                        "Nodo 1",
//                        "127.0.0.1:4000",
//
//
//
//                )
//        );

    }
}
