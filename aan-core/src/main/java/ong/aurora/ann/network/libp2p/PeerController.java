package ong.aurora.ann.network.libp2p;

import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ong.aurora.ann.network.AANNetworkNodeStatusType;
import ong.aurora.commons.serialization.AANSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class PeerController implements ProtocolMessageHandler<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(PeerController.class);

    Stream stream;

    CompletableFuture<PeerController> ready;

    AANSerializer annSerializer;

    public BehaviorSubject<AANNetworkNodeStatusType> connectionStatus =  BehaviorSubject.create(AANNetworkNodeStatusType.DISCONNECTED);

    public PublishSubject<Object> peerMessageSubject = PublishSubject.create();


    public PeerController(CompletableFuture<PeerController> ready, AANSerializer annSerializer) {
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

//    public void sendMessage(MessageType messageType, Object message) {
//        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);
//        String messages = annSerializer.toJSON(message);
//        P2PMessage p2PMessage = new P2PMessage(messageType, messages);
//        String messages2 = annSerializer.toJSON(p2PMessage);
//        ByteBuf byteBuf = Unpooled.wrappedBuffer(messages2.getBytes(StandardCharsets.UTF_8));
//        stream.writeAndFlush(byteBuf);
//    }

    public void sendMessage2(Object message) {
        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);
        String messages = annSerializer.toJSON(message);
        libp2pMessage p2PMessage = new libp2pMessage(message.getClass(), messages);
        String messages2 = annSerializer.toJSON(p2PMessage);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(messages2.getBytes(StandardCharsets.UTF_8));
        stream.writeAndFlush(byteBuf);
    }

    @Override
    public void onActivated(@NotNull Stream stream) {
        log.info("Nueva conexión establecida con {} {}", stream.remotePeerId(), stream.getConnection().remoteAddress());
        stream.getProtocol().thenAccept(s -> log.info("Protocolo: {}", s));
        this.stream = stream;
        connectionStatus.onNext(AANNetworkNodeStatusType.CONNECTED);
        this.ready.complete(this);
    }


    @Override
    public void onMessage(@NotNull Stream stream, ByteBuf rawData) {


//        P2PMessage encodedMessage = annSerializer.fromJSON(msg.toString(StandardCharsets.UTF_8), P2PMessage.class);
        try {
            libp2pMessage aanMessage = annSerializer.fromJSON(rawData.toString(StandardCharsets.UTF_8), libp2pMessage.class);
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
        connectionStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);
    }

    @Override
    public void onException(@Nullable Throwable cause) {
        log.info("onException", cause);
        connectionStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);
    }


}
