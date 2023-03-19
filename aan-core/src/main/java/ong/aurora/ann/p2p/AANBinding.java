package ong.aurora.ann.p2p;

import io.libp2p.core.multistream.StrictProtocolBinding;
import ong.aurora.ann.PeerController;
import ong.aurora.ann.p2p_2.AANNetworkPeer;
import ong.aurora.ann.p2p_2.libp2pNetworkPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AANBinding extends StrictProtocolBinding<libp2pNetworkPeer> {

    private static final Logger log = LoggerFactory.getLogger(AANBinding.class);

    public AANBinding(AANProtocol chatProtocol) {
        super(AANProtocol.announce, chatProtocol);
        log.info("Chat binding {} {}", AANProtocol.announce, chatProtocol.toString());
    }


}
