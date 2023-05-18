package ong.aurora.aan.core.network;

import kotlin.Pair;
import ong.aurora.aan.blockchain.AANBlockchain;
import ong.aurora.aan.command.Command;
import ong.aurora.aan.core.network.message.BlockchainBalancerBlockRequestMessage;
import ong.aurora.aan.core.network.message.BlockchainBalancerBlockResponseMessage;
import ong.aurora.aan.core.network.message.BlockchainUpdaterBlockResponseMessage;
import ong.aurora.aan.core.network.message.SendCommandMessage;
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

public class AANNetworkNode {


    private static final Logger logger = LoggerFactory.getLogger(AANNetworkNode.class);
    public AANNodeValue aanNodeValue;

    private final BehaviorSubject<AANNetworkNodeStatusType> nodeStatus = BehaviorSubject.create(AANNetworkNodeStatusType.DISCONNECTED);

    public AANNetworkNodeStatusType currentStatus() {
        return nodeStatus.getValue();
    }

    Long nodeBlockchainIndex = -1L;

    @Override
    public String toString() {
        return "AANNetworkNode{" +
                "aanNodeValue=" + aanNodeValue.nodeId() +
                ", nodeStatus=" + nodeStatus.getValue() +
                ", blockchainIndex=" + nodeBlockchainIndex +
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
            this.terminateNodeConnection();
        }
        this.peerConnection = peerConnection;
        this.nodeStatus.onNext(AANNetworkNodeStatusType.CONNECTED);
        onPeerMessageSubscription = this.peerConnection.onPeerMessage().doOnCompleted(this::terminateNodeConnection).subscribe(o -> {
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


            if (o instanceof SendCommandMessage message) {
                this.onCommandRequest.onNext(message.command());
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
    private final PublishSubject<Command> onCommandRequest = PublishSubject.create();

    public Observable<Pair<AANNetworkNode, Long>> onBlockchainBalancerBlockRequestStream() {
        return onBlockchainBalancerBlockRequest.map(aLong -> new Pair<>(this, aLong));
    }

    public Observable<Pair<AANNetworkNode, Command>> onCommandRequestStream() {
        return onCommandRequest.map(command -> new Pair<>(this, command));
    }

    PublishSubject<Event> onBlockchainBalancerBlockResponse = PublishSubject.create();

    public CompletableFuture<Event> sendRequestBlock(Long blockId) {
        CompletableFuture<Event> cc = new CompletableFuture<>();
        this.peerConnection.sendMessage(new BlockchainBalancerBlockRequestMessage(blockId));
        onBlockchainBalancerBlockResponse
                .takeFirst(event -> Objects.equals(event.eventId(), blockId))
//                .timeout(15, TimeUnit.SECONDS)
//                .onE
//                .doOnError(throwable -> {
//                    logger.info("sendRequestBlock Error detectado {} ", throwable.getCause());
//                })
                .subscribe(cc::complete);
        return cc;
    }


    public void sendBlockchainBalancerRespondBlock(Event e) {
        this.peerConnection.sendMessage(new BlockchainBalancerBlockResponseMessage(e));
    }

    private void onBlockchainUpdaterBlockResponse(BlockchainUpdaterBlockResponseMessage message) {
        if (message.blockchainIndex().compareTo(this.nodeBlockchainIndex) != 0) {
            this.nodeBlockchainIndex = message.blockchainIndex();
            this.nodeStatus.onNext(this.nodeStatus.getValue());
        }
    }

    public void terminateNodeConnection() {
        logger.info("[{}] Finalizando conexión {}", this.aanNodeValue.nodeId(), this);
        this.nodeBlockchainIndex = -1L;
        this.nodeStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);
        this.peerConnection.closeConnection();
        if (onPeerMessageSubscription != null) {
            onPeerMessageSubscription.unsubscribe();
        }
    }

    public void sendCommand(Command c) {
        this.peerConnection.sendMessage(new SendCommandMessage(c));
    }



    public void sendPeerMessage(AANNetworkMessage m) {
        peerConnection.sendMessage(m);
    }

}
