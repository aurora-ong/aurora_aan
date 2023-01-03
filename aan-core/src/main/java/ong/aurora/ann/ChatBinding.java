package ong.aurora.ann;

import io.libp2p.core.P2PChannelHandler;
import io.libp2p.core.multistream.StrictProtocolBinding;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatBinding extends StrictProtocolBinding<Chatter> {

    private static final Logger log = LoggerFactory.getLogger(ChatProtocol.class);

    public ChatBinding(ChatProtocol chatProtocol) {
        super(ChatProtocol.announce, chatProtocol);
        log.info("Chat binding {} {}", ChatProtocol.announce, chatProtocol.toString());
    }


}
