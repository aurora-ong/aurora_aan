package ong.aurora.ann;

import io.libp2p.core.P2PChannel;
import io.libp2p.core.Stream;
import io.libp2p.core.multistream.ProtocolBinding;
import io.libp2p.core.multistream.ProtocolDescriptor;
import io.libp2p.protocol.ProtocolHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ProtocolB extends ProtocolHandler<TestP> {

    private static final Logger log = LoggerFactory.getLogger(ProtocolB.class);


    public ProtocolB(long initiatorTrafficLimit, long responderTrafficLimit) {
        super(initiatorTrafficLimit, responderTrafficLimit);
    }

    @NotNull
    @Override
    protected CompletableFuture<TestP> onStartInitiator(@NotNull Stream stream) {
        return super.onStartInitiator(stream);
    }

    @NotNull
    @Override
    protected CompletableFuture<TestP> onStartResponder(@NotNull Stream stream) {
        return super.onStartResponder(stream);
    }

    @Override
    protected void initProtocolStream(@NotNull Stream stream) {
        super.initProtocolStream(stream);
    }
}
