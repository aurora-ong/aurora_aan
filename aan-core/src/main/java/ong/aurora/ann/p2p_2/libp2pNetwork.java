package ong.aurora.ann.p2p_2;

import io.libp2p.core.Host;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.Builder;
import io.libp2p.crypto.keys.RsaPrivateKey;
import ong.aurora.ann.AANConfig;
import ong.aurora.ann.PeerController;
import ong.aurora.ann.p2p.*;
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
        AANProtocol protocol = new AANProtocol(onNetworkConnection, annSerializer);
        AANBinding aanBinding = new AANBinding(protocol);

        this.libp2pHost = new Builder()
                .protocols(protocolBindings -> {
                    protocolBindings.add(aanBinding);
                    return null;
                })
                .network(networkConfigBuilder -> {
                    String add = "/dns4/".concat("localhost").concat("/tcp/".concat(aanConfig.getNetworkNodePort().toString()));
                    networkConfigBuilder.listen(add);
                    return null;
                })
                .identity(identityBuilder -> {
                    identityBuilder.setFactory(() -> {

                        PrivKey privKey = new RsaPrivateKey(aanConfig.getPrivateKey(), aanConfig.getPublicKey());

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
