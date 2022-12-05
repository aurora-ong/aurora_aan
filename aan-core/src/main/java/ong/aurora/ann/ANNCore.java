package ong.aurora.ann;

import ong.aurora.commons.blockchain.ANNBlockchain;
import ong.aurora.commons.command.Command;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.store.file.FileEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
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

        FileEventStore eventSource = new FileEventStore("events.ann");

        ANNBlockchain blockchain = new ANNBlockchain(eventSource);

        if (blockchain.isEmpty()) {
            log.info("!! Blockchain no inicializada");

        } else{
            log.info("!! Blockchain inicializada ({} bloques)", blockchain.blockCount());
            log.info("Verificando integridad");
            blockchain.verifyIntegrity().get();

        }


//        try {
//
//            long eventCount = eventSource.readEventStore().count();
//            log.info("Cargando {} eventos", eventCount);
//            eventSource.saveEvent(new Event("0", "eventtest", Map.of(), Instant.now(), "hash123", new Command(Instant.now(), "commandTest", Map.of()))).get();
//
////            eventSource.readEventStore().iterator()
//            long eventCount2 = eventSource.readEventStore().count();
//            log.info("Cargando {} eventos", eventCount2);
//
//
//            eventSource.readEventStore().forEachOrdered(event -> {
//                log.info("Leyendo eventStore {}", event);
//            });
//
//
//
//
//        } catch (Exception e) {
//            log.error("Escribiendo error", e);
//        }

        // TODO CHECKEAR ESTADO BLOCKCHAIN
        // TODO OBTENER NODOS ACTIVOS

//
//        ANNNodeIdentity nodeIdentity;
//        try {
//            nodeIdentity = new ANNNodeIdentity(nodeId);
//        } catch (Exception e) {
//            log.error("No se pudo obtener la identidad del nodo", e);
//            e.printStackTrace();
//        }

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
