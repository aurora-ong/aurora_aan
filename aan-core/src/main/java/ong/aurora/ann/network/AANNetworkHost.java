package ong.aurora.ann.network;

import kotlin.Pair;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.event.Event;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

public class AANNetworkHost {

    AANNetwork aanNetwork;
    BehaviorSubject<List<AANNetworkNode>> networkNodes;

    BehaviorSubject<AANNetworkHostStatusType> nodeStatus = BehaviorSubject.create(AANNetworkHostStatusType.DISCONNECTED);

    private static final Logger logger = LoggerFactory.getLogger(AANNetworkHost.class);

    AANBlockchain aanBlockchain;

    Subscription reconnectionSubscription;

    Subscription x, z;

    Scheduler schedulerExecutor2 = Schedulers.from(Executors.newSingleThreadExecutor());

    public AANNetworkHost(BehaviorSubject<List<AANNetworkNode>> networkNodes, AANNetwork aanNetwork, AANBlockchain aanBlockchain, Scheduler schedulerExecutor) {
        this.networkNodes = networkNodes;
        this.aanNetwork = aanNetwork;
        this.aanBlockchain = aanBlockchain;
//
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//        schedulerExecutor2 = Schedulers.from(executorService);


        // RECONNECTION LISTENER
        networkStatusListener()
                .observeOn(rx.schedulers.Schedulers.newThread())
                .subscribe(this::connectionListener);

        // BLOCKCHAIN UPDATER
        x = Observable.combineLatest(networkStatusListener(), aanBlockchain.lastEventStream, Pair::new)
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::blockchainUpdate);

        // BALANCEADOR DE BLOQUES
        z = Observable.combineLatest(networkStatusListener(), aanBlockchain.lastEventStream, Pair::new)
                .observeOn(schedulerExecutor)
                .subscribe(this::doBlockchainBalance);

        //
        this.networkBlockchainRequestListener()
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::onBlockchainUpdateRequest);

        // NETWORK LISTENER
        networkStatusListener()
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::connectionPrinter);

    }

    Observable<List<AANNetworkNode>> networkStatusListener() {
        return networkNodes.asObservable()
                .switchMap(aanNetworkNodes -> {
                    if (aanNetworkNodes.isEmpty()) {
                        return Observable.never();
                    }
                    return Observable.combineLatest(aanNetworkNodes.stream().map(AANNetworkNode::onStatusChange).toList(), args1 -> (List<AANNetworkNode>) (List<?>) Arrays.stream(args1).toList());

                })

                .doOnNext(aanNetworkNodes -> logger.info("++ networkStatus updated"))
                .observeOn(schedulerExecutor2)
                .throttleLast(1, TimeUnit.SECONDS, schedulerExecutor2)
                .doOnNext(aanNetworkNodes -> logger.info("++ networkStatus updated throttled"))
                .share();

    }

    Observable<Pair<AANNetworkNode, Long>> networkBlockchainRequestListener() {
        return networkNodes.asObservable().flatMap(aanNetworkNodes -> Observable.merge(aanNetworkNodes.stream().map(AANNetworkNode::onRequestBlock).toList()), (aanNetworkNodes, o) -> o);
    }

    private void doBlockchainBalance(Pair<List<AANNetworkNode>, Event> pair) {

        List<AANNetworkNode> aanNetworkNodes = pair.component1();

        if (aanNetworkNodes.stream().allMatch(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED)) {
            logger.info("[blockchainBalance] No hay nodos disponibles");
            return;
        }

        // TODO COMPROBAR TODOS NODOS CONECTADOS Y BALANCEADOS ?

        long currentEventIndex = Optional.ofNullable(pair.component2()).map(Event::eventId).orElse(-1L);

        Optional<AANNetworkNode> n = aanNetworkNodes.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED).filter(aanNetworkNode -> aanNetworkNode.latestBlockchainIndex != null).filter(aanNetworkNode -> aanNetworkNode.latestBlockchainIndex > currentEventIndex).findFirst();

        if (n.isPresent()) {
            logger.info("[blockchainBalance] Solicitando evento {} a {}", currentEventIndex + 1, n.get().aanNodeValue.nodeId());
//                Event a = n.get().sendRequestBlock(currentEventIndex + 1).orTimeout(5, TimeUnit.SECONDS).join();
            try {
//                Event a = n.get().sendRequestBlock(currentEventIndex + 1).orTimeout(5, TimeUnit.SECONDS).join();
                Event a = n.get().sendRequestBlock(currentEventIndex + 1).join();

//                logger.info("Sleep blockchainBalance");
//                Thread.sleep(1000);
                aanBlockchain.persistEvent(a).join();
//                Thread.sleep(1000);
//                logger.info(" -- Sleep blockchainBalance");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

//                n.get().sendRequestBlock(currentEventIndex + 1).thenAccept(event -> {
//                    try {
//                        aanBlockchain.persistEvent(event).join();
////                        Thread.sleep(10000);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                });
        } else {
            logger.info("[blockchainBalance] Blockchain balanceada ?");
        }
    }

    private void connectionListener(List<AANNetworkNode> aanNetworkNodes) {
        logger.info("[networkConnection] Ejecutando");
        if (aanNetworkNodes.isEmpty()) {
            logger.info("isEmpty");
            return;
        }

        boolean disconected = aanNetworkNodes.stream().anyMatch(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED);

        if (disconected) {
            if (this.reconnectionSubscription == null || this.reconnectionSubscription.isUnsubscribed()) {
//                 ACTIVAR RECONEXIÓN PERIODICA
                logger.info("[networkConnection] Reconexión activada");
                this.reconnectionSubscription = Observable
                        .interval(15, TimeUnit.SECONDS)
                        .subscribe(aLong -> {
                            logger.info("Intentando reconectar ({} intento)", aLong + 1);
                            this.networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetwork::establishConnection);

                        });
            }

        } else {
            // DESACTIVAR

            if (reconnectionSubscription != null && !reconnectionSubscription.isUnsubscribed()) {
                logger.info("[networkConnection] Reconexión desactivada");
                reconnectionSubscription.unsubscribe();
            }

        }
    }

    private void blockchainUpdate(Pair<List<AANNetworkNode>, Event> pair) {
        logger.info("[blockchainUpdate] Ejecutando");
        List<AANNetworkNode> networkNodeList = pair.component1();

        Long eventId = Optional.ofNullable(pair.component2()).map(Event::eventId).orElse(-1L);

        List<AANNetworkNode> notificableNodes = networkNodeList.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() != AANNetworkNodeStatusType.DISCONNECTED).toList();

        if (notificableNodes.isEmpty()) {
            logger.info("[blockchainUpdate] No hay nodos para enviar actualización");
            return;
        }

        // ENVIAR ACTUALIZACIONES DE BLOCKCHAIN HACÍA LA RED
        notificableNodes.forEach(aanNetworkNode -> {
            logger.info("[blockchainUpdate] Enviando actualización a {} @ {} ({})", aanNetworkNode.aanNodeValue.nodeId(), eventId);
            aanNetworkNode.sendBlockchainReport(eventId);
        });
    }

    private void onBlockchainUpdateRequest(Pair<AANNetworkNode, Long> pair) {
        logger.info("[{}] Ha solicitado bloque {}", pair.component1().aanNodeValue.nodeId(), pair.component2());
        try {
            Event event = aanBlockchain.eventStream().filter(e -> Objects.equals(e.eventId(), pair.component2())).findFirst().orElseThrow(() -> new Exception("Event not found"));
            pair.component1().sendRespondBlock(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void connectionPrinter(List<AANNetworkNode> aanNetworkNodes) {
        logger.info("======= Red actualizada =======");
        aanNetworkNodes.forEach(aanNetworkNode -> logger.info(aanNetworkNode.toString()));
        logger.info("=======");
    }


}