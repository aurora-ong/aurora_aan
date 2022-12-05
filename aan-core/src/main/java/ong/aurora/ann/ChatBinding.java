package ong.aurora.ann;

import io.libp2p.core.P2PChannelHandler;
import io.libp2p.core.multistream.StrictProtocolBinding;
import org.jetbrains.annotations.NotNull;

public class ChatBinding extends StrictProtocolBinding<ChatController> {

    public ChatBinding(ChatProtocol chatProtocol) {
        super(ChatProtocol.announce, chatProtocol);
    }


}
