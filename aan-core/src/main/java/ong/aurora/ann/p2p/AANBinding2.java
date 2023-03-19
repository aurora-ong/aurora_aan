package ong.aurora.ann.p2p;

import io.libp2p.core.multistream.StrictProtocolBinding;
import ong.aurora.ann.PeerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AANBinding2 extends StrictProtocolBinding<PeerController> {

    private static final Logger log = LoggerFactory.getLogger(AANBinding2.class);

    public AANBinding2(AANProtocol2 chatProtocol) {
        super(AANProtocol.announce, chatProtocol);
        log.info("Chat binding {} {}", AANProtocol.announce, chatProtocol.toString());
    }


}
