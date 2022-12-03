package ong.aurora.ann;

import io.libp2p.core.P2PChannel;
import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolHandler;
import io.libp2p.protocol.ProtocolMessageHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ChatProtocol extends ProtocolHandler<ChatController> {

    private static final Logger log = LoggerFactory.getLogger(ChatProtocol.class);

    public static final String announce = "/example/chat/0.1.0";


    public ChatProtocol(long initiatorTrafficLimit, long responderTrafficLimit) {
        super(Long.MAX_VALUE, Long.MAX_VALUE);
    }

    @NotNull
    @Override
    protected CompletableFuture<ChatController> onStartInitiator(@NotNull Stream stream) {
//        return super.onStartInitiator(stream);
        log.info("onStartInitiator");
        return onStart(stream);
    }

    @NotNull
    @Override
    protected CompletableFuture<ChatController> onStartResponder(@NotNull Stream stream) {
//        return super.onStartResponder(stream);
        log.info("onStartResponder");
        return onStart(stream);
    }

    CompletableFuture<ChatController> onStart(Stream stream) {
        log.info("onStart");
        CompletableFuture<ChatController> ready = new CompletableFuture<>();
        Chatter chatController = new Chatter(ready);
        stream.pushHandler(chatController);

        return ready;
    }

    @NotNull
    @Override
    public CompletableFuture<ChatController> initChannel(@NotNull P2PChannel ch) {
        log.info("initChannel");
        return super.initChannel(ch);
    }
}
