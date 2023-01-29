package ong.aurora.ann.p2p;

import io.libp2p.core.PeerId;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.crypto.keys.RsaPublicKey;
import ong.aurora.ann.PeerController;
import ong.aurora.ann.identity.ANNNodeIdentity;
import ong.aurora.ann.p2p.msg.BlockchainStatus;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.peer.node.ANNNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

import static ong.aurora.commons.util.Utils.maybeUnsubscribe;

public class p2pPeerNode {

    private static final Logger log = LoggerFactory.getLogger(p2pPeerNode.class);

    public BehaviorSubject<p2pPeerStatus> peerStatus = BehaviorSubject.create(new p2pPeerStatus(null, P2PPeerConnectionStatusType.DISCONNECTED, null));

    p2pHostNode p2pHostNode;

    ANNNodeValue peerNodeValue;

    PeerController peerController;

    String peerId;

    Subscription peerStatusSubscription;
    Subscription peerMessageSubscription;


    public p2pPeerNode(p2pHostNode p2pHostNode, ANNNodeValue nodeValue) {
        this.p2pHostNode = p2pHostNode;
        this.peerNodeValue = nodeValue;

        RsaPublicKey rsaPublicKey = null;
        try {
            rsaPublicKey = new RsaPublicKey(ANNNodeIdentity.fromNodeSignature(peerNodeValue.nodeSignature()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.peerId = PeerId.fromPubKey(rsaPublicKey).toString();

        log.info("p2pPeerNode {} ({}@{})", nodeValue.nodeName(), nodeValue.nodeId(), nodeValue.nodeHostname());


    }

    public void localConnect(AANBinding chatBinding) {
        Multiaddr add = Multiaddr.fromString("/dns4/".concat(peerNodeValue.nodeHostname()).concat("/tcp/".concat(peerNodeValue.nodePort()).concat("/p2p/".concat(peerId))));

        try {
            peerController = chatBinding.dial(p2pHostNode.libp2pHost, add).getController().get();
            this.setPeerListener(peerController);

        } catch (Exception e) {
            log.info("Error al conectar con {} ({}@{})", this.peerNodeValue.nodeName(), this.peerNodeValue.nodeId(), this.peerNodeValue.nodeHostname());
        }
    }

    public void remoteConnect(PeerController peerController) {
        log.info("Conexión remota {} ({}@{})", this.peerNodeValue.nodeName(), this.peerNodeValue.nodeId(), this.peerNodeValue.nodeHostname());
        this.peerController = peerController;
        this.setPeerListener(peerController);
    }

    private void setPeerListener(PeerController chatController) {

        maybeUnsubscribe(peerStatusSubscription);

        peerMessageSubscription = chatController.peerMessageSubject.subscribe(this::processPeerMessage);

        peerStatusSubscription = chatController.connectionStatus.subscribe(p2PPeerConnectionStatusType -> {

            log.info("Actualizando estado peer {} {}", this.peerNodeValue.nodeId(), p2PPeerConnectionStatusType);
            this.peerStatus.onNext(new p2pPeerStatus(this.peerNodeValue.nodeId(), p2PPeerConnectionStatusType, null));

//            if (p2PPeerConnectionStatusType == P2PPeerConnectionStatusType.CONNECTED) {
//
//            }
//
//            if (p2PPeerConnectionStatusType == P2PPeerConnectionStatusType.DISCONNECTED) {
//                maybeUnsubscribe(peerMessageSubscription);
//            }

        });

    }

    public void dispose() {
        log.info("Dispose {} ({}@{})", this.peerNodeValue.nodeName(), this.peerNodeValue.nodeId(), this.peerNodeValue.nodeHostname());
        maybeUnsubscribe(peerStatusSubscription);
        maybeUnsubscribe(peerMessageSubscription);
    }


    private void processPeerMessage(Object message) {
        if (this.peerStatus.getValue().currentStatus() == P2PPeerConnectionStatusType.DISCONNECTED) {
            log.info("Ignorando mensaje (desconectado) {} ({}@{})", this.peerNodeValue.nodeName(), this.peerNodeValue.nodeId(), this.peerNodeValue.nodeHostname());
            return;
        }
        log.info("Procesando mensaje {} ({}@{})", this.peerNodeValue.nodeName(), this.peerNodeValue.nodeId(), this.peerNodeValue.nodeHostname());

        if (message instanceof BlockchainStatus blockchainStatusUpdate) {
//            log.info("Actualizando Blockchain {}", s);

            Long localBlockchainIndex = this.p2pHostNode.hostBlockchain.lastEvent().map(Event::eventId).orElse(-1L);

            if (localBlockchainIndex.compareTo(blockchainStatusUpdate.blockchainIndex()) == 0) {
                log.info("++ Nodo {} sincronizado remoteIndex: {} localIndex: {}", this.peerNodeValue.nodeId(), blockchainStatusUpdate.blockchainIndex(), localBlockchainIndex);
                this.peerStatus.onNext(new p2pPeerStatus(this.peerStatus.getValue().nodeId(), P2PPeerConnectionStatusType.READY, blockchainStatusUpdate.blockchainIndex()));
            } else {
                log.info("++ Nodo {} des-sincronizado remoteIndex: {} localIndex: {}", this.peerNodeValue.nodeId(), blockchainStatusUpdate.blockchainIndex(), localBlockchainIndex);
                this.peerStatus.onNext(new p2pPeerStatus(this.peerStatus.getValue().nodeId(), P2PPeerConnectionStatusType.BALANCING, blockchainStatusUpdate.blockchainIndex()));
            }


//            log.info("Blockchain actualizado");
        }
    }


}
