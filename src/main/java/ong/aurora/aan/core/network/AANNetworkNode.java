package ong.aurora.aan.core.network;

import kotlin.Pair;
import ong.aurora.aan.blockchain.AANBlockchain;
import ong.aurora.aan.core.network.message.BlockchainBalancerBlockRequestMessage;
import ong.aurora.aan.core.network.message.BlockchainBalancerBlockResponseMessage;
import ong.aurora.aan.core.network.message.BlockchainUpdaterBlockResponseMessage;
import ong.aurora.aan.event.Event;
import ong.aurora.aan.node.AANNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AANNetworkNode {


    private static final Logger logger = LoggerFactory.getLogger(AANNetworkNode.class);
    public AANNodeValue aanNodeValue;

    private final BehaviorSubject<AANNetworkNodeStatusType> nodeStatus = BehaviorSubject.create(AANNetworkNodeStatusType.DISCONNECTED);

    public AANNetworkNodeStatusType currentStatus() {
        return nodeStatus.getValue();
    }

    Long currentBlockchainIndex;

    @Override
    public String toString() {
        return "AANNetworkNode{" +
                "aanNodeValue=" + aanNodeValue.nodeId() +
                ", nodeStatus=" + nodeStatus.getValue() +
                ", blockchainIndex=" + currentBlockchainIndex +
                '}';
    }

    public AANNetworkPeer peerConnection;

    public AANBlockchain aanBlockchain;


    public AANNetworkNode(AANNodeValue aanNodeValue, AANBlockchain aanBlockchain) {
        this.aanNodeValue = aanNodeValue;
        this.aanBlockchain = aanBlockchain;
    }

    public void attachConnection(AANNetworkPeer peerConnection) {
        logger.info("Nueva conexión desde {} ({}:{})", this.aanNodeValue.nodeId(), this.aanNodeValue.nodeHostname(), this.aanNodeValue.nodePort());

        if (this.peerConnection != null) {

            logger.error("[{}] Nodo ya se encuentra con una conexión abierta", this.aanNodeValue.nodeId());
            throw new RuntimeException("Nodo ya se encuentra con una conexión abierta");

//            peerConnection.closeConnection();
//            return;
        }
        this.peerConnection = peerConnection;
        this.nodeStatus.onNext(AANNetworkNodeStatusType.CONNECTED);
        onPeerMessageSubscription = this.peerConnection.onPeerMessage().doOnCompleted(() -> {
            logger.info("[{}] Finalizando conexión {}", this.aanNodeValue.nodeId(), this.peerConnection.getPeerIdentity());
            this.currentBlockchainIndex = null;
            this.nodeStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);
            this.peerConnection = null;
            if (onPeerMessageSubscription != null) {
                onPeerMessageSubscription.unsubscribe();
            }
        }).subscribe(o -> {
            logger.info("[{}] Procesando mensaje {}", this.aanNodeValue.nodeId(), o.toString());

            if (o instanceof BlockchainUpdaterBlockResponseMessage message) {
                onBlockchainUpdaterBlockResponse(message);
            }

            if (o instanceof BlockchainBalancerBlockRequestMessage message) {
                this.onBlockchainBalancerBlockRequest.onNext(message.blockchainIndex());
            }

            if (o instanceof BlockchainBalancerBlockResponseMessage message) {
                this.onBlockchainBalancerBlockResponse.onNext(message.event());
            }
        });
        logger.info("Conexión establecida con {} ({}:{})", this.aanNodeValue.nodeId(), this.aanNodeValue.nodeHostname(), this.aanNodeValue.nodePort());

    }

    Subscription onPeerMessageSubscription;


   public Observable<AANNetworkNode> onStatusChange() {
        return this.nodeStatus.asObservable().map(aanNetworkNodeStatusType -> this);
    }

    public void sendBlockchainReport(Long eventId) {
        this.peerConnection.sendMessage(new BlockchainUpdaterBlockResponseMessage(eventId));
    }

    private final PublishSubject<Long> onBlockchainBalancerBlockRequest = PublishSubject.create();

    public Observable<Pair<AANNetworkNode, Long>> onBlockchainBalancerBlockRequestStream() {
        return onBlockchainBalancerBlockRequest.map(aLong -> new Pair<>(this, aLong));
    }

    PublishSubject<Event> onBlockchainBalancerBlockResponse = PublishSubject.create();

    public CompletableFuture<Event> sendRequestBlock(Long blockId) {
        CompletableFuture<Event> cc = new CompletableFuture<>();
        this.peerConnection.sendMessage(new BlockchainBalancerBlockRequestMessage(blockId));
        onBlockchainBalancerBlockResponse
                .takeFirst(event -> Objects.equals(event.eventId(), blockId))
                .timeout(15, TimeUnit.SECONDS)
                .doOnError(throwable -> {
                    logger.info("sendRequestBlock Error detectado {} ", throwable.getCause());
                })
                .subscribe(cc::complete);
        return cc;
    }


    public void sendRespondBlock(Event e) {
        this.peerConnection.sendMessage(new BlockchainBalancerBlockResponseMessage(e));

    }

    private void onBlockchainUpdaterBlockResponse(BlockchainUpdaterBlockResponseMessage message) {
        if (!Objects.equals(message.blockchainIndex(), this.currentBlockchainIndex)) {
            this.currentBlockchainIndex = message.blockchainIndex();
            this.nodeStatus.onNext(this.nodeStatus.getValue());
        }
    }

    public void terminateNodeConnection() {
        logger.info("[{}] Finalizando conexión {}", this.aanNodeValue.nodeId(), this);
        this.peerConnection.closeConnection();
    }

//    private void sendPeerMessage(Object m) {
//        if (nodeStatus.getValue() != AANNetworkNodeStatusType.DISCONNECTED && this.peerConnection != null) {
//            peerConnection.sendMessage(m);
//        } else {
//            logger.info("[{}] Intentando enviar mensaje a nodo desconectado {}", this.aanNodeValue.nodeId(), m.getClass());
//        }
//    }

}
