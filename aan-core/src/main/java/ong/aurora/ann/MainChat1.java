package ong.aurora.ann;


import io.libp2p.core.Host;
import io.libp2p.core.PeerId;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.crypto.keys.RsaPublicKey;
import ong.aurora.ann.identity.ANNNodeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainChat1 {

    private static final Logger log = LoggerFactory.getLogger(MainChat1.class);

    public static void main(String[] args) throws Exception {
        Chat chat1 = new Chat();



        String otherNodeAddr = "/ip4/127.0.0.1/tcp/4000";

        String nodeInfoPath = "";
        String nodeId = "";

        ANNNodeIdentity annNodeIdentity = ANNNodeIdentity.fromFile(nodeInfoPath.concat(nodeId).concat("/identity_private.pem"), nodeInfoPath.concat(nodeId).concat("/identity_public.pem"));

        log.info("PK {} {}", annNodeIdentity.publicKey.getEncoded().length, annNodeIdentity.publicKey.getEncoded());

        RsaPublicKey rsaPublicKey = new RsaPublicKey(annNodeIdentity.publicKey);

        PeerId peerId = PeerId.fromPubKey(rsaPublicKey);

        log.info("Peer id de otro host {}", peerId);

        Host thisNode = P2PNode.normal(chat1, "/ip4/127.0.0.1/tcp/3000");

        thisNode.start().get();
        log.info("Escuchando en: {}", thisNode.listenAddresses());

        ChatController chatController = chat1.dial(thisNode, peerId, Multiaddr.fromString(otherNodeAddr)).getController().get();

        chatController.send("Holiwi");

//        thisNode.stop().get();

    }
}