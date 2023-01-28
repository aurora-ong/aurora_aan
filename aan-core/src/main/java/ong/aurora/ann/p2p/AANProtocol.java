package ong.aurora.ann.p2p;

import io.libp2p.core.P2PChannel;
import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolHandler;
import ong.aurora.ann.PeerController;
import ong.aurora.commons.serialization.ANNSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.BehaviorSubject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AANProtocol extends ProtocolHandler<PeerController> {

    private static final Logger log = LoggerFactory.getLogger(AANProtocol.class);

    public static final String announce = "/aurora/aan/0.1.0";

    BehaviorSubject<List<p2pPeerNode>> networkPeers;

    ANNSerializer annSerializer;

    public AANProtocol(BehaviorSubject<List<p2pPeerNode>> networkPeers, ANNSerializer annSerializer) {
        super(Long.MAX_VALUE, Long.MAX_VALUE);
        this.networkPeers = networkPeers;
        this.annSerializer = annSerializer;
    }

    @NotNull
    @Override
    protected CompletableFuture<PeerController> onStartInitiator(@NotNull Stream stream) {
        log.info("onStartInitiator {}", stream.remotePeerId());
        CompletableFuture<PeerController> ready = new CompletableFuture<>();
        PeerController chatController = new PeerController(ready, annSerializer);
        stream.pushHandler(chatController);

//        chatController.

//        chatController.connectionStatus.asObservable().filter().first().toBlocking().

//        return chatController.connectionStatus.asObservable().filter(p2PPeerConnectionStatus -> p2PPeerConnectionStatus == P2PPeerConnectionStatus.CONNECTED).toBlocking().



        return ready;
    }

    @NotNull
    @Override
    protected CompletableFuture<PeerController> onStartResponder(@NotNull Stream stream) {
        log.info("onStartResponder {}", stream.remotePeerId());

        CompletableFuture<PeerController> ready = new CompletableFuture<>();
        PeerController chatController = new PeerController(ready, annSerializer);
        stream.pushHandler(chatController);
        return ready.thenApply(peerController -> {
            Optional<p2pPeerNode> nodePeerOptional = networkPeers.getValue().stream().filter(p2PPeerNode -> Objects.equals(p2PPeerNode.peerId, stream.remotePeerId().toString())).findFirst();

            if (nodePeerOptional.isEmpty()) {
                stream.close();
                throw new RuntimeException("El nodo no está registrado en esta blockchain");
            }
            p2pPeerNode nodePeer = nodePeerOptional.get();
            nodePeer.remoteConnect(chatController);

            return peerController;
        });
    }

    @NotNull
    @Override
    public CompletableFuture<PeerController> initChannel(@NotNull P2PChannel ch) {
        log.info("initChannel");
        return super.initChannel(ch);
    }

}
