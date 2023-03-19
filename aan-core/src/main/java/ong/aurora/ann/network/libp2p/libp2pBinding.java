package ong.aurora.ann.network.libp2p;

import io.libp2p.core.multistream.StrictProtocolBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class libp2pBinding extends StrictProtocolBinding<libp2pNetworkPeer> {

    private static final Logger log = LoggerFactory.getLogger(libp2pBinding.class);

    public libp2pBinding(libp2pProtocol chatProtocol) {
        super(libp2pProtocol.announce, chatProtocol);
        log.info("Chat binding {} {}", libp2pProtocol.announce, chatProtocol.toString());
    }


}
