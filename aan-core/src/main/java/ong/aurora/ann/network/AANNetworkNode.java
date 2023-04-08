package ong.aurora.ann.network;

import ong.aurora.ann.network.message.BlockchainRequest;
import ong.aurora.ann.network.message.BlockchainRespond;
import ong.aurora.ann.network.message.RespondBlock;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.peer.node.AANNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AANNetworkNode {


    private static final Logger logger = LoggerFactory.getLogger(AANNetworkNode.class);
    public AANNodeValue aanNodeValue;

    private BehaviorSubject<AANNetworkNodeStatusType> nodeStatus = BehaviorSubject.create(AANNetworkNodeStatusType.DISCONNECTED);

    AANNetworkNodeStatusType currentStatus() {
        return nodeStatus.getValue();
    }

    Long nodeBlockchainIndex;

    @Override
    public String toString() {
        return "AANNetworkNode{" +
                "aanNodeValue=" + aanNodeValue.nodeId() +
                ", nodeStatus=" + nodeStatus.getValue() +
                ", blockchainIndex=" + nodeBlockchainIndex +
                ", peerConnection=" + peerConnection +
                '}';
    }

    public AANNetworkPeer peerConnection;

    public AANBlockchain aanBlockchain;


    public AANNetworkNode(AANNodeValue aanNodeValue, AANNetworkPeer peerConnection, AANBlockchain aanBlockchain) {
        this.aanNodeValue = aanNodeValue;
        this.aanBlockchain = aanBlockchain;
    }

    public void attachConnection(AANNetworkPeer peerConnection) {
        logger.info("Conexión establecida con {} ({}:{})", this.aanNodeValue.nodeId(), this.aanNodeValue.nodeHostname(), this.aanNodeValue.nodePort());
//        if (peerConnection != null) {
//            throw new Exception("Ya se había asignado conexión");
//        }

        this.peerConnection = peerConnection;
        this.peerConnection.onPeerDisconected().subscribe(unused -> {
            this.nodeStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);
        });
        this.peerConnection.onPeerMessage().subscribe(o -> {
            logger.info("[{}] Procesando mensaje {}", this.aanNodeValue.nodeId(), o.getClass());
            if (o instanceof BlockchainRequest message) {
                responder(message);
            }
            if (o instanceof BlockchainRespond message) {
                responder(message);
            }

            if (o instanceof RespondBlock message) {
                responder(message);
            }
        });
        this.nodeStatus.onNext(AANNetworkNodeStatusType.CONNECTED);
//        sendBlockchain();
    }

    public Observable<AANNetworkNode> onStatusChange() {
        return this.nodeStatus.asObservable().map(aanNetworkNodeStatusType -> this);
    }

    private CompletableFuture<Void> confirmBlockchain() {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        peerConnection.sendMessage(new BlockchainRequest());

        return cf;
    }

    private void blockchainRequest() {
        logger.info("Enviando BlockchainRequest");
        peerConnection.sendMessage(new BlockchainRequest());
    }


    public void sendBlockchain(Optional<Event> event) {
        logger.info("Informando blockchain {}", event.orElse(null));
        peerConnection.sendMessage(new BlockchainRespond(event.map(Event::eventId).orElse(-1L)));
    }

    private void requestBlock() {
        peerConnection.sendMessage(new BlockchainRequest());

    }



    private void sendBlock() {
        try {
            Event eventt = aanBlockchain.eventStream().filter(event -> event.eventId() == (this.nodeBlockchainIndex +1) ).findFirst().orElseThrow(() -> new Exception("Event not found"));
            peerConnection.sendMessage(new RespondBlock(eventt));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void responder(BlockchainRequest message) {
        logger.info("[Mensaje] BlockchainRequest {}", this.aanBlockchain.lastEvent().orElse(null));
        peerConnection.sendMessage(new BlockchainRespond(this.aanBlockchain.lastEvent().map(Event::eventId).orElse(-1L)));
    }

    private void responder(BlockchainRespond message) {
        logger.info("Respondiendo BlockchainRespond {}", message);
        if (!Objects.equals(this.nodeBlockchainIndex, message.blockchainIndex())) {
            this.nodeBlockchainIndex = message.blockchainIndex();
            this.nodeStatus.onNext(this.nodeStatus.getValue());

        }
//        Long thisBlockchainIndex = aanBlockchain.lastEvent().map(event -> event.eventId()).orElse(-1L);
//        if (!Objects.equals(message.blockchainIndex(), thisBlockchainIndex)) {
//            this.nodeStatus.onNext(AANNetworkNodeStatusType.BALANCING);
//            if(thisBlockchainIndex > this.nodeBlockchainIndex) {
//                sendBlock();
//            }
//        } else {
//            this.nodeStatus.onNext(AANNetworkNodeStatusType.READY);
//        }
    }

    private void responder(RespondBlock message) {
        logger.info("Respuesta balancing block {}", message);
        try {
            this.aanBlockchain.persistEvent(message.event()).join();
//            sendBlockchain();
        } catch (Exception e) {
        }

    }

    public void closeNode() {
        this.peerConnection.closeConnection();
    }

}
