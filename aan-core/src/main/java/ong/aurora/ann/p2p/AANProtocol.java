package ong.aurora.ann.p2p;

import io.libp2p.core.P2PChannel;
import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolHandler;
import ong.aurora.ann.Chatter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AANProtocol extends ProtocolHandler<Chatter> {

    private static final Logger log = LoggerFactory.getLogger(AANProtocol.class);

    public static final String announce = "/aurora/aan/0.1.0";

    BehaviorSubject<List<AANP2PNodePeer>> networkPeers;

    public AANProtocol(BehaviorSubject<List<AANP2PNodePeer>> networkPeers) {
        super(Long.MAX_VALUE, Long.MAX_VALUE);
        this.networkPeers = networkPeers;
    }

    @NotNull
    @Override
    protected CompletableFuture<Chatter> onStartInitiator(@NotNull Stream stream) {
        log.info("onStartInitiator {}", stream.remotePeerId());
        CompletableFuture<Chatter> ready = new CompletableFuture<>();
        Chatter chatController = new Chatter(ready);
        stream.pushHandler(chatController);

        return ready;
    }

    @NotNull
    @Override
    protected CompletableFuture<Chatter> onStartResponder(@NotNull Stream stream) {
        log.info("onStartResponder {}", stream.remotePeerId());

        CompletableFuture<Chatter> ready = new CompletableFuture<>();
        Chatter chatController = new Chatter(ready);
        stream.pushHandler(chatController);
        return ready.thenApply(chatter -> {
            Optional<AANP2PNodePeer> nodePeerOptional = networkPeers.getValue().stream().filter(aanp2PNodePeer -> Objects.equals(aanp2PNodePeer.peerId, stream.remotePeerId().toString())).findFirst();

            if (nodePeerOptional.isEmpty()) {
                stream.close();
                throw new RuntimeException("El nodo no está registrado en esta blockchain");
            }
            AANP2PNodePeer nodePeer = nodePeerOptional.get();
            nodePeer.fromProtocol(chatController);

            return chatter;
        });
    }

    @NotNull
    @Override
    public CompletableFuture<Chatter> initChannel(@NotNull P2PChannel ch) {
        log.info("initChannel");
        return super.initChannel(ch);
    }

}
