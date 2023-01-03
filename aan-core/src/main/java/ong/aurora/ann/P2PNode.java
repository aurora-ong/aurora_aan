package ong.aurora.ann;

import io.libp2p.core.Host;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.Builder;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.crypto.keys.RsaPrivateKey;
import ong.aurora.ann.identity.ANNNodeIdentity;
import ong.aurora.ann.p2p.AANP2PNodePeer;
import ong.aurora.commons.peer.node.ANNNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class P2PNode {

    Host p2pHost;

    List<AANP2PNodePeer> hostPeers = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(P2PNode.class);

    public P2PNode(ANNNodeIdentity annNodeIdentity, ANNNodeValue nodeValue) {

        this.p2pHost = new Builder().protocols(protocolBindings -> {
//                    protocolBindings.add(chat);
                    return null;
                }).network(networkConfigBuilder -> {
                    String add = "/dns4/".concat(nodeValue.nodeHostname()).concat("/tcp/".concat(nodeValue.nodePort()));
                    networkConfigBuilder.listen(add);
                    return null;
                })
                .identity(identityBuilder -> {
                    identityBuilder.setFactory(() -> {

                        PrivKey privKey = new RsaPrivateKey(annNodeIdentity.privateKey, annNodeIdentity.publicKey);

                        return privKey;
                    });
                    return null;
                })


                .build(Builder.Defaults.Standard);
        ;
    }

    public CompletableFuture<Void> start() {
        return p2pHost.start().thenAccept(unused -> log.info("Escuchando en: {}", p2pHost.listenAddresses()));
    }

    public CompletableFuture<Void> dialNodes(List<ANNNodeValue> nodeValueList) {


        this.hostPeers = nodeValueList.stream().map(nodeValue -> {

            return new AANP2PNodePeer(this.p2pHost, nodeValue);

        }).toList();

        return CompletableFuture.completedFuture(null);
    }

    public static Host normal(Chat chat, String listen) {
        Host thisNode = new HostBuilder()
                .protocol(chat)
//                .transport(WsTransport::new)
                .listen(listen)
                .build();

        return thisNode;
    }

    public static Host test(Chat chat, String listen, ANNNodeIdentity annNodeIdentity) {
        Host node = new Builder().protocols(protocolBindings -> {
                    protocolBindings.add(chat);
                    return null;
                }).network(networkConfigBuilder -> {
                    networkConfigBuilder.listen(listen);
                    return null;
                })
                .identity(identityBuilder -> {
                    identityBuilder.setFactory(() -> {

                        PrivKey privKey = new RsaPrivateKey(annNodeIdentity.privateKey, annNodeIdentity.publicKey);

                        return privKey;
                    });
                    return null;
                })


                .build(Builder.Defaults.Standard);

        return node;
    }

}
