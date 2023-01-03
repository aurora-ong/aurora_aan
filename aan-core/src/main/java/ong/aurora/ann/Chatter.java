package ong.aurora.ann;

import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class Chatter implements ProtocolMessageHandler<ByteBuf>, ChatController {

    private static final Logger log = LoggerFactory.getLogger(Chatter.class);

    Stream stream;

    CompletableFuture<Chatter> ready;


    public Chatter(CompletableFuture<Chatter> ready) {
        this.ready = ready;
    }

    @Override
    public void send(String message) {
        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);

        ByteBuf byteBuf = Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8));

        stream.writeAndFlush(byteBuf);

//        this.ready.whenComplete((chatter, throwable) -> {
//
//            ByteBuf byteBuf = Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8));
//
//            stream.writeAndFlush(byteBuf);
//        });

    }

    @Override
    public void onActivated(@NotNull Stream stream) {
        log.info("Nueva conexión establecida con {} {}", stream.remotePeerId(), stream.getConnection().remoteAddress().toString());
        stream.getProtocol().thenAccept(s -> log.info("Protocolo: {}", s));
        this.stream = stream;
        this.ready.complete(this);
    }


    @Override
    public void onMessage(@NotNull Stream stream, ByteBuf msg) {
//        ProtocolMessageHandler.super.onMessage(stream, msg);
        log.info("Mensaje recibido de {} {}", this.stream.getConnection().secureSession().getRemoteId(), msg.toString(StandardCharsets.UTF_8));
    }

    @Override
    public void onClosed(@NotNull Stream stream) {
//        ProtocolMessageHandler.super.onClosed(stream);
        log.info("onClosed");
    }

    @Override
    public void onException(@Nullable Throwable cause) {
//        ProtocolMessageHandler.super.onException(cause);
        log.info("onException", cause);
    }


}
