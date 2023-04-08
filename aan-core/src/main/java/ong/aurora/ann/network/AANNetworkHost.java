package ong.aurora.ann.network;

import kotlin.Pair;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class AANNetworkHost {

    AANNetwork aanNetwork;
    BehaviorSubject<List<AANNetworkNode>> networkNodes;

    BehaviorSubject<AANNetworkHostStatusType> nodeStatus = BehaviorSubject.create(AANNetworkHostStatusType.DISCONNECTED);

    private static final Logger logger = LoggerFactory.getLogger(AANNetworkHost.class);

    AANBlockchain aanBlockchain;

    Subscription reconnectionSubscription;

    public AANNetworkHost(BehaviorSubject<List<AANNetworkNode>> networkNodes, AANNetwork aanNetwork, AANBlockchain aanBlockchain) {
        this.networkNodes = networkNodes;
        this.aanNetwork = aanNetwork;
        this.aanBlockchain = aanBlockchain;

        // CONNECTION LISTENER

        statusListener().subscribe(aanNetworkNodes -> {

            if (aanNetworkNodes.isEmpty()) {
                logger.info("isEmpty");
            }

            boolean disconected = aanNetworkNodes.stream().anyMatch(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED);

            if (disconected) {
                if (this.reconnectionSubscription == null || this.reconnectionSubscription.isUnsubscribed()) {
//                 ACTIVAR RECONEXIÓN PERIODICA
                    logger.info("!! Reconexión activada");
                    this.reconnectionSubscription = Observable.interval(15, TimeUnit.SECONDS).subscribe(aLong -> {
                        logger.info("Intentando reconectar ({} intento)",aLong+1);
                        doReconnection();
                    });
                }

            } else {
                // DESACTIVAR
                logger.info("!! Reconexión desactivada");
                reconnectionSubscription.unsubscribe();
            }


        });

        statusListener().subscribe(aanNetworkNodes -> {
           logger.info("Status listener trigger");
        });

        aanBlockchain.lastEventStream.subscribe(event -> {
            logger.info("lastEventStream {}", event);
        });

        Observable.combineLatest(statusListener(), aanBlockchain.lastEventStream, Pair::new).subscribe(pair -> {
            logger.info("Trigger updateBlockchain");
            List<AANNetworkNode> networkNodeList = pair.component1();
            Event currentEvent = pair.component2();

            // ENVIAR ACTUALIZACIONES DE BLOCKCHAIN HACÍA LA RED
            networkNodeList.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() != AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetworkNode -> {
                logger.info("Informando a {} estado blockchain {} ({})", aanNetworkNode.aanNodeValue.nodeId(), currentEvent.eventId(), currentEvent.blockHash());
                aanNetworkNode.sendBlockchain(aanBlockchain.lastEvent());
            });

        });


        // BALANCEADOR DE BLOQUES

        statusListener().subscribe(aanNetworkNodes -> {

            Event currentEvent = this.aanBlockchain.lastEventStream.getValue();

            Optional<AANNetworkNode> n = aanNetworkNodes.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED).filter(aanNetworkNode -> aanNetworkNode.nodeBlockchainIndex != null).filter(aanNetworkNode -> aanNetworkNode.nodeBlockchainIndex > currentEvent.eventId()).findFirst();

            if (n.isPresent()) {
                logger.info("Rebalancear desde {}, solicitando evento {}", n.get().aanNodeValue.nodeId(), currentEvent.eventId() + 1);

            }

        });

//        Observable.c

//        aanBlockchain.onEventPersisted.asObservable().subscribe(event -> {
//           // ENVIAR ACTUALIZACIONES DE BLOCKCHAIN HACÍA LA RED
//            this.networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.nodeStatus.getValue() != AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetworkNode -> {
//                logger.info("Informando a {} estado blockchain {} ({})", aanNetworkNode.aanNodeValue.nodeId(), event.eventId(), event.blockHash());
//            });
//        });



//
//                .subscribe(o -> {
//            logger.info("\n======= Red actualizada =======");
//            o.forEach(aanNetworkNode -> logger.info(aanNetworkNode.toString()));
//
//            boolean rdy = o.stream().allMatch(aanNetworkNode -> aanNetworkNode.nodeStatus.getValue() == AANNetworkNodeStatusType.CONNECTED);
//
//            if (nodeStatus.getValue())
//
//
//            logger.info("==============");
//        });
    }


    Observable<List<AANNetworkNode>> statusListener() {
        return networkNodes.asObservable()
                .flatMap(aanNetworkNodes -> Observable.combineLatest(aanNetworkNodes.stream().map(AANNetworkNode::onStatusChange).toList(), args1 -> (List<AANNetworkNode>) (List<?>) Arrays.stream(args1).toList()), (aanNetworkNodes, o) -> aanNetworkNodes).asObservable();
    }

    private void doReconnection() {
        this.networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetwork::establishConnection);
    }

    ;
}