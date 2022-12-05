package ong.aurora.ann;

import io.libp2p.core.Host;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.Builder;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.crypto.keys.RsaPrivateKey;

public class P2PNode {


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
