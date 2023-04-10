package ong.aurora.ann.network;

import kotlin.Pair;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

import java.util.Arrays;
import java.util.List;
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

        // NETWORK LISTENER

        networkStatusListener().subscribe(aanNetworkNodes -> {
            logger.info("======= Red actualizada =======");
            aanNetworkNodes.forEach(aanNetworkNode -> logger.info(aanNetworkNode.toString()));
            logger.info("=======");

        });

//        this.networkStatusListener().timeout(15, TimeUnit.SECONDS).subscribe(aanNetworkNodes -> {
//
//            logger.info("dsadas");
//
//        } );
        // CONNECTION LISTENER

        networkStatusListener().subscribe(aanNetworkNodes -> {

            if (aanNetworkNodes.isEmpty()) {
                logger.info("isEmpty");
                return;
            }

            boolean disconected = aanNetworkNodes.stream().anyMatch(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED);

            if (disconected) {
                if (this.reconnectionSubscription == null || this.reconnectionSubscription.isUnsubscribed()) {
//                 ACTIVAR RECONEXIÓN PERIODICA
                    logger.info("!! Reconexión activada");
                    this.reconnectionSubscription = Observable.interval(15, TimeUnit.SECONDS).subscribe(aLong -> {
                        logger.info("Intentando reconectar ({} intento)", aLong + 1);
                        doReconnection();
                    });
                }

            } else {
                // DESACTIVAR
                logger.info("!! Reconexión desactivada");
                reconnectionSubscription.unsubscribe();
            }

        });

        aanBlockchain.lastEventStream.subscribe(event -> {
            logger.info("lastEventStream {}", event);
        });

        // BLOCKCHAIN INFORMER

        Observable.combineLatest(networkStatusListener(), aanBlockchain.lastEventStream, Pair::new).subscribe(pair -> {
            logger.info("Trigger updateBlockchain");
            List<AANNetworkNode> networkNodeList = pair.component1();
            Event currentEvent = pair.component2();

            // ENVIAR ACTUALIZACIONES DE BLOCKCHAIN HACÍA LA RED
            networkNodeList.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() != AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetworkNode -> {
                logger.info("Informando a {} estado blockchain {} ({})", aanNetworkNode.aanNodeValue.nodeId(), currentEvent.eventId(), currentEvent.blockHash());
                aanNetworkNode.sendBlockchainReport(aanBlockchain.lastEvent());
            });

        });

        // BALANCEADOR DE BLOQUES
//        networkStatusListener().flatMap()

        Observable.combineLatest(networkStatusListener(), aanBlockchain.lastEventStream, Pair::new).subscribe(pair -> {
            logger.info("Balancadeador de bloques triggered");

            List<AANNetworkNode> aanNetworkNodes =  pair.component1();

            Event currentEvent = pair.component2();

            Optional<AANNetworkNode> n = aanNetworkNodes.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED).filter(aanNetworkNode -> aanNetworkNode.nodeBlockchainIndex != null).filter(aanNetworkNode -> aanNetworkNode.nodeBlockchainIndex > currentEvent.eventId()).findFirst();

            if (n.isPresent()) {
                logger.info("Rebalancear desde {}, solicitando evento {}", n.get().aanNodeValue.nodeId(), currentEvent.eventId() + 1);
//               n.get().sendRequestBlock(currentEvent.eventId() + 1).thenAccept(event -> {
//
//                   logger.info("Evento obtenido {}", event);
//
//               });
                n.get().sendRequestBlock(currentEvent.eventId() + 1).thenAccept(event -> {
                    logger.info("X {}", event);
                    try {
                        aanBlockchain.persistEvent(event).join();
                        logger.info("L");
                        Thread.sleep(10000);
                        logger.info("M");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                });
                logger.info("Z");




            }

        });

        blStatusListener().subscribe(aanNetworkNodeLongPair -> {
            logger.info("Nodo {}, ha solicitado bloque {}", aanNetworkNodeLongPair.component1().aanNodeValue.nodeId(), aanNetworkNodeLongPair.component2());
            try {
                Event event = aanBlockchain.eventStream().filter(e -> e.eventId() == aanNetworkNodeLongPair.component2()).findFirst().orElseThrow(() -> new Exception("Event not found"));
                aanNetworkNodeLongPair.component1().sendRespondBlock(event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });



    }

    Observable<List<AANNetworkNode>> networkStatusListener() {

        return networkNodes.asObservable().switchMap(aanNetworkNodes -> {
            if (aanNetworkNodes.isEmpty()) {
                return Observable.just(List.of());

            }
            return Observable.combineLatest(aanNetworkNodes.stream().map(AANNetworkNode::onStatusChange).toList(), args1 -> (List<AANNetworkNode>) (List<?>) Arrays.stream(args1).toList());

        });

//        return networkNodes.asObservable().switchMap(aanNetworkNodes -> {
//            if (aanNetworkNodes.isEmpty()) {
//                return Observable.just(List.of());
//
//            }
//            return Observable.combineLatest(aanNetworkNodes.stream().map(AANNetworkNode::onStatusChange).toList(), args1 -> (List<AANNetworkNode>) (List<?>) Arrays.stream(args1).toList());
//
//        }, (aanNetworkNodes, o) -> aanNetworkNodes);
    }

    Observable<Pair<AANNetworkNode, Long>> blStatusListener() {
        return networkNodes.asObservable().flatMap(aanNetworkNodes -> Observable.merge(aanNetworkNodes.stream().map(AANNetworkNode::onRequestBlock).toList()), (aanNetworkNodes, o) -> o);
    }

    private void doReconnection() {
        this.networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetwork::establishConnection);
    }

    private Event onRequestBlock(AANNetworkNode node, Long eventId) {
        logger.info("Nodo {} ha solicitado bloque {}", node.aanNodeValue.nodeId(), eventId);
        try {
            return aanBlockchain.eventStream().filter(event -> event.eventId() == (eventId)).findFirst().orElseThrow(() -> new Exception("Event not found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    ;
}