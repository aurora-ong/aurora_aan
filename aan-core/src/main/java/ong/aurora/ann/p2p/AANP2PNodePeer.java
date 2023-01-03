package ong.aurora.ann.p2p;

import io.libp2p.core.Host;
import io.libp2p.core.PeerId;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.crypto.keys.RsaPublicKey;
import ong.aurora.ann.ChatBinding;
import ong.aurora.ann.ChatController;
import ong.aurora.ann.ChatProtocol;
import ong.aurora.ann.Chatter;
import ong.aurora.ann.identity.ANNNodeIdentity;
import ong.aurora.commons.peer.node.ANNNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.BehaviorSubject;

public class AANP2PNodePeer {

    private static final Logger log = LoggerFactory.getLogger(AANP2PNodePeer.class);

    public BehaviorSubject<P2PPeerStatus> peerStatus = BehaviorSubject.create(new P2PPeerStatus(P2PPeerStatusType.INITIAL));

    Host thisNodeHost;

    ANNNodeValue peerNodeValue;

    Chatter chatController;

    String peerId;


    public AANP2PNodePeer(Host host, ANNNodeValue nodeValue) {
        this.thisNodeHost = host;
        this.peerNodeValue = nodeValue;

        RsaPublicKey rsaPublicKey = null;
        try {
            rsaPublicKey = new RsaPublicKey(ANNNodeIdentity.fromNodeSignature(peerNodeValue.nodeSignature()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.peerId = PeerId.fromPubKey(rsaPublicKey).toString();

        log.info("P2PPeer Instanciando {} {} {} {}", nodeValue.nodeId(), nodeValue.nodeName(), nodeValue.nodeHostname(), peerId);


    }

    public void connect(AANBinding chatBinding) {
        Multiaddr add = Multiaddr.fromString("/dns4/".concat(peerNodeValue.nodeHostname()).concat("/tcp/".concat(peerNodeValue.nodePort()).concat("/p2p/".concat(peerId.toString()))));

        try {
//            Chatter chatController = this.dial(thisNodeHost, add).getController().get();

            chatController = chatBinding.dial(thisNodeHost, add).getController().get();

            peerStatus.onNext(new P2PPeerStatus(P2PPeerStatusType.READY));

        } catch (Exception e) {
//            log.error("Error al conectar con Peer {} {}", add.toString(), e);
            log.info("Error al conectar con Peer {}", add.toString());
            peerStatus.onNext(new P2PPeerStatus(P2PPeerStatusType.ERROR));
        }
    }

    public void fromProtocol(Chatter chatController) {
        log.info("Conectado desde externo {}", this.peerNodeValue.nodeId());
        this.chatController = chatController;
        peerStatus.onNext(new P2PPeerStatus(P2PPeerStatusType.READY));
    }
}
