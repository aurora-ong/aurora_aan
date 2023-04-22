package ong.aurora.ann.network.libp2p;

import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ong.aurora.ann.network.AANNetworkPeer;
import ong.aurora.ann.network.AANNetworkNodeStatusType;
import ong.aurora.commons.serialization.AANSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class libp2pNetworkPeer implements ProtocolMessageHandler<ByteBuf>, AANNetworkPeer {

    private static final Logger log = LoggerFactory.getLogger(libp2pNetworkPeer.class);

    Stream stream;

    CompletableFuture<libp2pNetworkPeer> ready;

    AANSerializer aanSerializer;

    public BehaviorSubject<AANNetworkNodeStatusType> connectionStatus = BehaviorSubject.create(AANNetworkNodeStatusType.DISCONNECTED);

    public PublishSubject<Object> onPeerMessage = PublishSubject.create();

    PublishSubject<Void> onPeerDisconnected = PublishSubject.create();


    public libp2pNetworkPeer(CompletableFuture<libp2pNetworkPeer> ready, AANSerializer aanSerializer) {
        this.ready = ready;
        this.aanSerializer = aanSerializer;
    }

    @Override
    public void onActivated(@NotNull Stream stream) {
//        log.info("Nueva conexión entrante {} {}", stream.remotePeerId(), stream.getConnection().remoteAddress());
        this.stream = stream;
        connectionStatus.onNext(AANNetworkNodeStatusType.CONNECTED);
        this.ready.complete(this);
    }


    @Override
    public void onMessage(@NotNull Stream stream, ByteBuf rawData) {

        try {
            libp2pMessage aanMessage = aanSerializer.fromJSON(rawData.toString(StandardCharsets.UTF_8), libp2pMessage.class);
//            log.info("Mensaje recibido desde {} {}", this.stream.getConnection().secureSession().getRemoteId(), aanMessage);
            Object a = aanSerializer.fromJSON(aanMessage.encodedMessage(), aanMessage.messageType());
            onPeerMessage.onNext(a);
        } catch (Exception e) {
            log.info("Error al decodificar mensaje {} {}", e, rawData.toString(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void onClosed(@NotNull Stream stream) {
        log.info("Conexión cerrada {}", this.stream.getConnection().secureSession().getRemoteId());
        connectionStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);
        this.onPeerDisconnected.onNext(null);
    }

    @Override
    public void onException(@Nullable Throwable cause) {
        log.info("onException", cause);
        connectionStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);
    }


    @Override
    public String getPeerIdentity() {
        return Base64.getEncoder().encodeToString(stream.getConnection().secureSession().getRemotePubKey().raw());
    }

    @Override
    public void sendMessage(Object message) {
//        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);
        String messages = aanSerializer.toJSON(message);
        libp2pMessage p2PMessage = new libp2pMessage(message.getClass(), messages);
        String messages2 = aanSerializer.toJSON(p2PMessage);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(messages2.getBytes(StandardCharsets.UTF_8));
        stream.writeAndFlush(byteBuf);
    }

    @Override
    public Observable<Object> onPeerMessage() {
        return onPeerMessage.asObservable();
    }

    @Override
    public Observable<Void> onPeerDisconected() {
        return onPeerDisconnected.asObservable();
    }

    @Override
    public void closeConnection() {
        this.stream.close();
    }
}
