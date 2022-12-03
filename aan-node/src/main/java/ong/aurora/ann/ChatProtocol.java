package ong.aurora.ann;

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
        super(initiatorTrafficLimit, responderTrafficLimit);
    }

    @NotNull
    @Override
    protected CompletableFuture<ChatController> onStartInitiator(@NotNull Stream stream) {
//        return super.onStartInitiator(stream);
        return onStart(stream);
    }

    @NotNull
    @Override
    protected CompletableFuture<ChatController> onStartResponder(@NotNull Stream stream) {
//        return super.onStartResponder(stream);
        return onStart(stream);
    }

    CompletableFuture<ChatController> onStart(Stream stream) {
        log.info("onStart");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Chatter chatController = new Chatter(completableFuture);
        stream.pushHandler(chatController);
        return completableFuture.thenApply(unused -> chatController);
    }
}
