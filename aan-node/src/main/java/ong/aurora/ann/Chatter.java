package ong.aurora.ann;

import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class Chatter implements ProtocolMessageHandler<String>, ChatController {

    private static final Logger log = LoggerFactory.getLogger(Chatter.class);

    Stream stream;

    CompletableFuture<Void> ready;

    public Chatter(CompletableFuture<Void> ready) {
        this.ready = ready;
    }

    @Override
    public void send(String message) {
        log.info("Enviar mensaje a {} {}", this.stream.remotePeerId(), message);
        stream.writeAndFlush(message.getBytes(Charset.defaultCharset()));
    }

    @Override
    public void onActivated(@NotNull Stream stream) {

//        ProtocolMessageHandler.super.onActivated(stream);
        log.info("onActivated {}", stream.remotePeerId());
        stream.getProtocol().thenAccept(s -> log.info("Get Protocol: {}", s));
        this.stream = stream;
        stream.writeAndFlush("LALA");

        this.ready.complete(null);
    }



    @Override
    public void onMessage(@NotNull Stream stream, String msg) {
        log.info("onMessage");

//        ProtocolMessageHandler.super.onMessage(stream, msg);
        log.info("Mensaje recibido de {} {}", this.stream.getConnection().secureSession().getLocalId(), msg);
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

    @Override
    public void fireMessage(@NotNull Stream stream, @NotNull Object msg) {
        log.info("fireMessage {}", msg.toString());
    }
}
