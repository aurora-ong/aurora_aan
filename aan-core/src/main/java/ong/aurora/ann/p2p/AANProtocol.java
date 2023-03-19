package ong.aurora.ann.p2p;

import io.libp2p.core.P2PChannel;
import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolHandler;
import ong.aurora.ann.PeerController;
import ong.aurora.ann.p2p_2.AANNetworkPeer;
import ong.aurora.ann.p2p_2.libp2pNetworkPeer;
import ong.aurora.commons.serialization.AANSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AANProtocol extends ProtocolHandler<libp2pNetworkPeer> {

    private static final Logger log = LoggerFactory.getLogger(AANProtocol.class);

    public static final String announce = "/aurora/aan/0.1.0";

    PublishSubject<AANNetworkPeer> onNetworkConnection;

    AANSerializer annSerializer;

    public AANProtocol(PublishSubject<AANNetworkPeer> onNetworkConnection, AANSerializer annSerializer) {
        super(Long.MAX_VALUE, Long.MAX_VALUE);
        this.onNetworkConnection = onNetworkConnection;
        this.annSerializer = annSerializer;
    }

    @NotNull
    @Override
    protected CompletableFuture<libp2pNetworkPeer> onStartInitiator(@NotNull Stream stream) {
        log.info("onStartInitiator {}", stream.remotePeerId());
        CompletableFuture<libp2pNetworkPeer> ready = new CompletableFuture<>();
        libp2pNetworkPeer chatController = new libp2pNetworkPeer(ready, annSerializer);
        stream.pushHandler(chatController);

        return ready.thenApply(peerController -> {
            this.onNetworkConnection.onNext(peerController);
            return peerController;
        });
    }

    @NotNull
    @Override
    protected CompletableFuture<libp2pNetworkPeer> onStartResponder(@NotNull Stream stream) {
        log.info("onStartResponder {}", stream.remotePeerId());

        CompletableFuture<libp2pNetworkPeer> ready = new CompletableFuture<>();
        libp2pNetworkPeer chatController = new libp2pNetworkPeer(ready, annSerializer);

        stream.pushHandler(chatController);
        return ready.thenApply(peerController -> {
            this.onNetworkConnection.onNext(peerController);
            return peerController;
        });
    }

    @NotNull
    @Override
    public CompletableFuture<libp2pNetworkPeer> initChannel(@NotNull P2PChannel ch) {
        log.info("initChannel");
        return super.initChannel(ch);
    }

}
