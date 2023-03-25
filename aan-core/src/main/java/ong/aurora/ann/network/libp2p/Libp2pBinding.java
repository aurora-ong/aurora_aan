package ong.aurora.ann.network.libp2p;

import io.libp2p.core.multistream.StrictProtocolBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Libp2pBinding extends StrictProtocolBinding<libp2pNetworkPeer> {

    private static final Logger log = LoggerFactory.getLogger(Libp2pBinding.class);

    public Libp2pBinding(Libp2pProtocol chatProtocol) {
        super(Libp2pProtocol.announce, chatProtocol);
        log.info("Chat binding {} {}", Libp2pProtocol.announce, chatProtocol.toString());
    }


}
