package ong.aurora.ann.p2p;

import io.libp2p.core.multistream.StrictProtocolBinding;
import ong.aurora.ann.ChatProtocol;
import ong.aurora.ann.Chatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AANBinding extends StrictProtocolBinding<Chatter> {

    private static final Logger log = LoggerFactory.getLogger(ChatProtocol.class);

    public AANBinding(AANProtocol chatProtocol) {
        super(AANProtocol.announce, chatProtocol);
        log.info("Chat binding {} {}", AANProtocol.announce, chatProtocol.toString());
    }


}