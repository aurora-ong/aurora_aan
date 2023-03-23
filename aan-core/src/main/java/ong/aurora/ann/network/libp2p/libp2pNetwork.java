package ong.aurora.ann.network.libp2p;

import io.libp2p.core.Host;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.Builder;
import io.libp2p.crypto.keys.RsaPrivateKey;
import ong.aurora.commons.config.AANConfig;
import ong.aurora.ann.network.AANNetwork;
import ong.aurora.ann.network.AANNetworkPeer;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.serialization.AANSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.util.List;

public class libp2pNetwork implements AANNetwork {

    Host libp2pHost;

    BehaviorSubject<List<PeerController>> networkPeers = BehaviorSubject.create(List.of());

    AANBlockchain hostBlockchain;

    PublishSubject<AANNetworkPeer> onNetworkConnection = PublishSubject.create();

    private static final Logger log = LoggerFactory.getLogger(libp2pNetwork.class);

    public libp2pNetwork(AANConfig aanConfig, AANSerializer annSerializer, AANBlockchain hostBlockchain) {
        this.hostBlockchain = hostBlockchain;
        libp2pProtocol protocol = new libp2pProtocol(onNetworkConnection, annSerializer);
        libp2pBinding libp2pBinding = new libp2pBinding(protocol);

        this.libp2pHost = new Builder()
                .protocols(protocolBindings -> {
                    protocolBindings.add(libp2pBinding);
                    return null;
                })
                .network(networkConfigBuilder -> {
                    String add = "/dns4/".concat("localhost").concat("/tcp/".concat(aanConfig.networkNodePort.toString()));
                    networkConfigBuilder.listen(add);
                    return null;
                })
                .identity(identityBuilder -> {
                    identityBuilder.setFactory(() -> {

                        PrivKey privKey = new RsaPrivateKey(aanConfig.privateKey, aanConfig.publicKey);

                        return privKey;
                    });
                    return null;
                })
//                .connectionHandlers(connectionHandlers -> {
//                    connectionHandlers.add(new ConnectionHandler() {
//                        @Override
//                        public void handleConnection(@NotNull Connection connection) {
//                            connection.muxerSession().
//                        }
//                    });
//                    return null;
//                })
                .build(Builder.Defaults.Standard);

    }

    @Override
    public void startHost() {
        libp2pHost.start().thenAccept(unused -> log.info("Escuchando en: {}", libp2pHost.listenAddresses()));

    }

    @Override
    public PublishSubject<AANNetworkPeer> onNetworkConnection() {
        return this.onNetworkConnection;
    }

//    BehaviorSubject<List<PeerController>> networkPeers = BehaviorSubject.create();


}
