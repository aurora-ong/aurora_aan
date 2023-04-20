package ong.aurora.ann.network;

import kotlin.Pair;
import ong.aurora.ann.network.message.BlockchainReport;
import ong.aurora.ann.network.message.RequestBlock;
import ong.aurora.ann.network.message.RespondBlock;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.peer.node.AANNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AANNetworkNode {


    private static final Logger logger = LoggerFactory.getLogger(AANNetworkNode.class);
    public AANNodeValue aanNodeValue;

    private BehaviorSubject<AANNetworkNodeStatusType> nodeStatus = BehaviorSubject.create(AANNetworkNodeStatusType.DISCONNECTED);

    public AANNetworkNodeStatusType currentStatus() {
        return nodeStatus.getValue();
    }

    Long latestBlockchainIndex;

    @Override
    public String toString() {
        return "AANNetworkNode{" +
                "aanNodeValue=" + aanNodeValue.nodeId() +
                ", nodeStatus=" + nodeStatus.getValue() +
                ", blockchainIndex=" + latestBlockchainIndex +
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
        logger.info("Nueva conexión desde {} ({}:{})", this.aanNodeValue.nodeId(), this.aanNodeValue.nodeHostname(), this.aanNodeValue.nodePort());

        //        if (peerConnection != null) {
//            throw new Exception("Ya se había asignado conexión");
//        }

        if (this.peerConnection != null) {
            try {
                throw new Exception("Nodo ya se encuentra con una conexión abierta");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        this.peerConnection = peerConnection;
        startPeerSubscribers();
        this.nodeStatus.onNext(AANNetworkNodeStatusType.CONNECTED);
        logger.info("Conexión establecida con {} ({}:{})", this.aanNodeValue.nodeId(), this.aanNodeValue.nodeHostname(), this.aanNodeValue.nodePort());

    }


    Subscription onPeerDisconnectedSubscription;
    Subscription onPeerMessageSubscription;

    private void startPeerSubscribers() {
        logger.info("[{}] Comenzando subscripción peerConnection", aanNodeValue.nodeId());
        this.nodeStatus.subscribe(nodeStatus -> {
           logger.info("[{}] nodeStatus actualizado", this);
        });
        onPeerDisconnectedSubscription = this.peerConnection.onPeerDisconected().subscribe(unused -> {

            this.latestBlockchainIndex = null;
            this.peerConnection = null;
            clearPeerSubscription();
            this.nodeStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);

        });
        onPeerMessageSubscription = this.peerConnection.onPeerMessage().subscribe(o -> {
            logger.info("[{}] Procesando mensaje {}", this.aanNodeValue.nodeId(), o.toString());
            if (o instanceof RequestBlock message) {
//                onRequestBlock(message);
                this.onRequestBlockPS.onNext(message.blockchainIndex());
            }
            if (o instanceof BlockchainReport message) {
                onBlockchainReport(message);
            }

            if (o instanceof RespondBlock message) {
                this.onRespondBlockPS.onNext(message.event());
            }
        });
    }

    private void clearPeerSubscription() {
        onPeerMessageSubscription.unsubscribe();
        onPeerDisconnectedSubscription.unsubscribe();
    }

    public Observable<AANNetworkNode> onStatusChange() {
        return this.nodeStatus.asObservable().map(aanNetworkNodeStatusType -> this);
    }

//    private CompletableFuture<Void> confirmBlockchain() {
//        CompletableFuture<Void> cf = new CompletableFuture<>();
//        peerConnection.sendMessage(new BlockchainRequest());
//
//        return cf;
//    }

//    private void blockchainRequest() {
//        logger.info("Enviando BlockchainRequest");
//        peerConnection.sendMessage(new BlockchainRequest());
//    }


    public void sendBlockchainReport(Long eventId) {
        logger.info("Informando blockchain {}", eventId);
        peerConnection.sendMessage(new BlockchainReport(eventId));
    }


    private PublishSubject<Long> onRequestBlockPS = PublishSubject.create();

    public Observable<Pair<AANNetworkNode, Long>> onRequestBlock() {
        return onRequestBlockPS.map(aLong -> new Pair<>(this, aLong));
    }



    PublishSubject<Event> onRespondBlockPS = PublishSubject.create();

    public CompletableFuture<Event> sendRequestBlock(Long blockId) {

        CompletableFuture<Event> cc = new CompletableFuture<>();

        peerConnection.sendMessage(new RequestBlock(blockId));
        onRespondBlockPS.takeFirst(event -> Objects.equals(event.eventId(), blockId)).subscribe(cc::complete);

//        onRespondBlockPS.filter(event -> event.eventId() == blockId).single().subscribe(a -> {
//            logger.info("LLegó 2 {}", a);
//
//        });

        return cc;
    }


    public void sendRespondBlock(Event e) {
        peerConnection.sendMessage(new RespondBlock(e));

    }

//    private void responder(BlockchainRequest message) {
//        logger.info("[Mensaje] BlockchainRequest {}", this.aanBlockchain.lastEvent().orElse(null));
//        peerConnection.sendMessage(new BlockchainReport(this.aanBlockchain.lastEvent().map(Event::eventId).orElse(-1L)));
//    }

    private void onBlockchainReport(BlockchainReport message) {
        logger.info("onBlockchainReport {} {}", message, this.latestBlockchainIndex);
        if (!Objects.equals(message.blockchainIndex(), this.latestBlockchainIndex)) {
            logger.info("onBlockchainReport activado");
            this.latestBlockchainIndex = message.blockchainIndex();
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

    private void onRequestBlock(RequestBlock message) {
        logger.info("onRequestBlock {}", message);

//        if (!Objects.equals(this.nodeBlockchainIndex, message.blockchainIndex())) {
//            this.nodeBlockchainIndex = message.blockchainIndex();
//            this.nodeStatus.onNext(this.nodeStatus.getValue());
//
//        }
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

//    private void responder(RespondBlock message) {
//        logger.info("Respuesta balancing block {}", message);
//        try {
//            this.aanBlockchain.persistEvent(message.event()).join();
////            sendBlockchain();
//        } catch (Exception e) {
//        }
//
//    }

    public void closeNode() {
        logger.info("[{}] Finalizando conexión {}", this.aanNodeValue.nodeId(), this);
        this.peerConnection.closeConnection();
    }

}
