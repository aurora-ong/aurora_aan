package ong.aurora.aan.core.network;

import kotlin.Pair;
import ong.aurora.aan.blockchain.AANBlockchain;
import ong.aurora.aan.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AANNetworkHost {

    AANNetwork aanNetwork;
    BehaviorSubject<List<AANNetworkNode>> networkNodes;

    private static final Logger logger = LoggerFactory.getLogger(AANNetworkHost.class);

    AANBlockchain aanBlockchain;

    Scheduler networkStatusScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    public AANNetworkHost(BehaviorSubject<List<AANNetworkNode>> networkNodes, AANNetwork aanNetwork, AANBlockchain aanBlockchain, Scheduler schedulerExecutor) {
        this.networkNodes = networkNodes;
        this.aanNetwork = aanNetwork;
        this.aanBlockchain = aanBlockchain;

        // RECONNECTION LISTENER
        networkStatusListener()
                .map(aanNetworkNodes -> aanNetworkNodes.stream().anyMatch(node -> node.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED))
                .distinctUntilChanged()
                .switchMap(this::reconnectionObserver)
                .subscribeOn(networkStatusScheduler)
                .subscribe();

        // BLOCKCHAIN UPDATER
        Observable.combineLatest(networkStatusListener(), aanBlockchain.lastEventStream, Pair::new)
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::blockchainUpdater);

        // BLOCKCHAIN BALANCER
        Observable.combineLatest(networkStatusListener(), aanBlockchain.lastEventStream, Pair::new)
                .observeOn(schedulerExecutor)
                .subscribe(this::blockchainBalancer);

        // BLOCKCHAIN BALANCER RESPONSER
        this.networkBlockchainRequestListener()
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::blockchainBalancerServer);

        // NETWORK LOGGER
        networkStatusListener()
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::networkLogger);

    }

    Observable<List<AANNetworkNode>> networkStatusListener() {
        return networkNodes.asObservable()
                .observeOn(networkStatusScheduler)
                .switchMap(aanNetworkNodes -> {
                    if (aanNetworkNodes.isEmpty()) {
                        return Observable.never();
                    }
                    return Observable.combineLatest(aanNetworkNodes.stream().map(AANNetworkNode::onStatusChange).toList(), args1 -> (List<AANNetworkNode>) (List<?>) Arrays.stream(args1).toList());

                })
                .throttleLast(1, TimeUnit.SECONDS, networkStatusScheduler)
                .share();

    }

    Observable<Pair<AANNetworkNode, Long>> networkBlockchainRequestListener() {
        return networkNodes.asObservable().flatMap(aanNetworkNodes -> Observable.merge(aanNetworkNodes.stream().map(AANNetworkNode::onBlockchainBalancerBlockRequestStream).toList()), (aanNetworkNodes, o) -> o);
    }

    private void blockchainBalancer(Pair<List<AANNetworkNode>, Event> pair) {

        List<AANNetworkNode> aanNetworkNodes = pair.component1();

        if (aanNetworkNodes.stream().allMatch(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED)) {
            logger.info("[blockchainBalance] No hay nodos disponibles");
            return;
        }

        // TODO COMPROBAR TODOS NODOS CONECTADOS Y BALANCEADOS ?

        long currentEventIndex = Optional.ofNullable(pair.component2()).map(Event::eventId).orElse(-1L);

        Optional<AANNetworkNode> n = aanNetworkNodes.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED).filter(aanNetworkNode -> aanNetworkNode.currentBlockchainIndex != null).filter(aanNetworkNode -> aanNetworkNode.currentBlockchainIndex > currentEventIndex).findFirst();

        if (n.isPresent()) {
            logger.info("[blockchainBalance] Solicitando evento {} a {}", currentEventIndex + 1, n.get().aanNodeValue.nodeId());
            try {
                Event a = n.get().sendRequestBlock(currentEventIndex + 1).join();
                aanBlockchain.persistEvent(a).join();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("[blockchainBalance] Blockchain balanceada ?");
        }
    }

    private Observable reconnectionObserver(boolean networkDisconnected) {

        {
            logger.info("[networkConnection] Red desconectada ? {}", networkDisconnected);

            if (!networkDisconnected) {
                logger.info("[networkConnection] Reconexión desactivada");
                return Observable.never();
            }
            logger.info("[networkConnection] Reconexión activada");

            return Observable
                    .interval(1, 15, TimeUnit.SECONDS)
                    .doOnNext(aLong -> {
                        logger.info("[networkConnection] Intentando reconectar ({} intento)", aLong + 1);
                        this.networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetwork::establishConnection);

                    });

        }

    }

    private void blockchainUpdater(Pair<List<AANNetworkNode>, Event> pair) {
        logger.info("[blockchainUpdater] Ejecutando");
        List<AANNetworkNode> networkNodeList = pair.component1();

        Long eventId = Optional.ofNullable(pair.component2()).map(Event::eventId).orElse(-1L);

        List<AANNetworkNode> notificableNodes = networkNodeList.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() != AANNetworkNodeStatusType.DISCONNECTED).toList();

        if (notificableNodes.isEmpty()) {
            logger.info("[blockchainUpdater] No hay nodos para enviar actualización");
            return;
        }

        // ENVIAR ACTUALIZACIONES DE BLOCKCHAIN HACÍA LA RED
        notificableNodes.forEach(aanNetworkNode -> {
            logger.info("[blockchainUpdater] Enviando actualización a {} @ {} ({})", aanNetworkNode.aanNodeValue.nodeId(), eventId);
            aanNetworkNode.sendBlockchainReport(eventId);
        });
    }

    private void blockchainBalancerServer(Pair<AANNetworkNode, Long> pair) {
        logger.info("[blockchainBalancerServer] Nodo {} ha solicitado bloque {}", pair.component1().aanNodeValue.nodeId(), pair.component2());
        try {
            Event event = aanBlockchain.eventStream().filter(e -> Objects.equals(e.eventId(), pair.component2())).findFirst().orElseThrow(() -> new Exception("Event not found"));
            pair.component1().sendRespondBlock(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void networkLogger(List<AANNetworkNode> aanNetworkNodes) {
        logger.info("======= Red actualizada =======");
        aanNetworkNodes.forEach(aanNetworkNode -> logger.info(aanNetworkNode.toString()));
        logger.info("=======");
    }


}