package ong.aurora.ann.p2p_2;

import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ong.aurora.ann.p2p.P2PPeerConnectionStatusType;
import ong.aurora.ann.p2p.msg.MessageType;
import ong.aurora.ann.p2p.msg.P2PMessage;
import ong.aurora.ann.p2p.msg.P2PMessage2;
import ong.aurora.commons.serialization.AANSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class libp2pNetworkPeer implements ProtocolMessageHandler<ByteBuf>, AANNetworkPeer {

    private static final Logger log = LoggerFactory.getLogger(libp2pNetworkPeer.class);

    Stream stream;

    CompletableFuture<libp2pNetworkPeer> ready;

    AANSerializer annSerializer;

    public BehaviorSubject<P2PPeerConnectionStatusType> connectionStatus =  BehaviorSubject.create(P2PPeerConnectionStatusType.DISCONNECTED);

    public PublishSubject<Object> peerMessageSubject = PublishSubject.create();


    public libp2pNetworkPeer(CompletableFuture<libp2pNetworkPeer> ready, AANSerializer annSerializer) {
        this.ready = ready;
        this.annSerializer = annSerializer;
    }

    public void send(String message) {
        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);

        ByteBuf byteBuf = Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8));

        stream.writeAndFlush(byteBuf);

//        this.ready.whenComplete((chatter, throwable) -> {
//
//            ByteBuf byteBuf = Unpooled.wrappedBuffer(encodedMessage.getBytes(StandardCharsets.UTF_8));
//
//            stream.writeAndFlush(byteBuf);
//        });



    }

    public void sendMessage(MessageType messageType, Object message) {
        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);
        String messages = annSerializer.toJSON(message);
        P2PMessage p2PMessage = new P2PMessage(messageType, messages);
        String messages2 = annSerializer.toJSON(p2PMessage);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(messages2.getBytes(StandardCharsets.UTF_8));
        stream.writeAndFlush(byteBuf);
    }

    public void sendMessage2(Object message) {
        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);
        String messages = annSerializer.toJSON(message);
        P2PMessage2 p2PMessage = new P2PMessage2(message.getClass(), messages);
        String messages2 = annSerializer.toJSON(p2PMessage);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(messages2.getBytes(StandardCharsets.UTF_8));
        stream.writeAndFlush(byteBuf);
    }

    @Override
    public void onActivated(@NotNull Stream stream) {
        log.info("Nueva conexión establecida con {} {}", stream.remotePeerId(), stream.getConnection().remoteAddress());
        stream.getProtocol().thenAccept(s -> log.info("Protocolo: {}", s));
        this.stream = stream;
        stream.getConnection().secureSession().getRemotePubKey().bytes();
        connectionStatus.onNext(P2PPeerConnectionStatusType.CONNECTED);
        this.ready.complete(this);
    }




    @Override
    public void onMessage(@NotNull Stream stream, ByteBuf rawData) {


//        P2PMessage encodedMessage = annSerializer.fromJSON(msg.toString(StandardCharsets.UTF_8), P2PMessage.class);
        try {
            P2PMessage2 aanMessage = annSerializer.fromJSON(rawData.toString(StandardCharsets.UTF_8), P2PMessage2.class);
            log.info("Mensaje recibido desde {} {}", this.stream.getConnection().secureSession().getRemoteId(), aanMessage);
            Object a = annSerializer.fromJSON(aanMessage.encodedMessage(), aanMessage.messageType());
            peerMessageSubject.onNext(a);
        } catch (Exception e) {
            log.info("Error al decodificar mensaje {} {}", e, rawData.toString(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void onClosed(@NotNull Stream stream) {
        log.info("onClosed");
        connectionStatus.onNext(P2PPeerConnectionStatusType.DISCONNECTED);
    }

    @Override
    public void onException(@Nullable Throwable cause) {
        log.info("onException", cause);
        connectionStatus.onNext(P2PPeerConnectionStatusType.DISCONNECTED);
    }


    @Override
    public String getPeerIdentity() {
        return stream.getConnection().secureSession().getRemotePubKey().toString();
    }

    @Override
    public void sendMessage(Object message) {
        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);
        String messages = annSerializer.toJSON(message);
        P2PMessage2 p2PMessage = new P2PMessage2(message.getClass(), messages);
        String messages2 = annSerializer.toJSON(p2PMessage);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(messages2.getBytes(StandardCharsets.UTF_8));
        stream.writeAndFlush(byteBuf);
    }

    @Override
    public PublishSubject<Object> onPeerMessage() {
        return null;
    }

    @Override
    public PublishSubject<Void> onPeerDisconected() {
        return null;
    }
}
