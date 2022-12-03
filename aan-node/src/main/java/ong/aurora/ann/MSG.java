package ong.aurora.ann;

import io.libp2p.core.Stream;
import io.libp2p.protocol.ProtocolMessageHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MSG implements ProtocolMessageHandler<TestP> {

    private static final Logger log = LoggerFactory.getLogger(MSG.class);

    @Override
    public void onActivated(@NotNull Stream stream) {
        ProtocolMessageHandler.super.onActivated(stream);
        log.info("Activated");
    }

    @Override
    public void onMessage(@NotNull Stream stream, TestP msg) {
        ProtocolMessageHandler.super.onMessage(stream, msg);
        log.info("onMessage");
    }

    @Override
    public void fireMessage(@NotNull Stream stream, @NotNull Object msg) {
        ProtocolMessageHandler.super.fireMessage(stream, msg);
        log.info("Fire MSG");
    }

    @Override
    public void onException(@Nullable Throwable cause) {
        ProtocolMessageHandler.super.onException(cause);
        log.info("Exceotion {}", cause);
    }
}
