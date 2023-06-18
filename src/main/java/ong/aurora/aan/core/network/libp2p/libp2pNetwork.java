package ong.aurora.aan.core.network.libp2p;

import io.libp2p.core.Host;
import io.libp2p.core.PeerId;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.Builder;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.crypto.keys.RsaPrivateKey;
import io.libp2p.crypto.keys.RsaPublicKey;
import ong.aurora.aan.blockchain.AANBlockchain;
import ong.aurora.aan.config.AANConfig;
import ong.aurora.aan.core.network.AANNetwork;
import ong.aurora.aan.core.network.AANNetworkNode;
import ong.aurora.aan.core.network.AANNetworkPeer;
import ong.aurora.aan.serialization.AANSerializer;
import ong.aurora.aan.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.PublishSubject;

import java.util.concurrent.CompletableFuture;

public class libp2pNetwork implements AANNetwork {

    Host libp2pHost;

    ong.aurora.aan.core.network.libp2p.libp2pBinding libp2pBinding;

    AANBlockchain hostBlockchain;

    PublishSubject<AANNetworkPeer> onNetworkConnection = PublishSubject.create();

    private static final Logger log = LoggerFactory.getLogger(libp2pNetwork.class);

    public libp2pNetwork(AANConfig aanConfig, AANSerializer annSerializer, AANBlockchain hostBlockchain) {
        this.hostBlockchain = hostBlockchain;
        libp2pProtocol protocol = new libp2pProtocol(onNetworkConnection, annSerializer);
        this.libp2pBinding = new libp2pBinding(protocol);


        this.libp2pHost = new Builder()
                .protocols(protocolBindings -> {
                    protocolBindings.add(libp2pBinding);
                    return null;
                })
                .network(networkConfigBuilder -> {
                    String add = "/dns4/".concat("localhost").concat("/tcp/".concat(aanConfig.nodePort.toString()));
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
    public CompletableFuture<Void> startHost() {
        return libp2pHost.start().thenAccept(unused -> log.info("Escuchando en: {}", libp2pHost.listenAddresses()));
    }

    @Override
    public PublishSubject<AANNetworkPeer> onNetworkConnection() {
        return this.onNetworkConnection;
    }

    @Override
    public void establishConnection(AANNetworkNode aanNetworkNode) {
        if (aanNetworkNode.isAnonymous()) {
            throw new RuntimeException("No se puede establecer conexión con un nodo anónimo");
        }

        try {
            RsaPublicKey rsaPublicKey = new RsaPublicKey(Utils.publicKeyFromString(aanNetworkNode.aanNodeValue.nodeSignature()));
            PeerId peerId = PeerId.fromPubKey(rsaPublicKey);
            Multiaddr add = Multiaddr.fromString("/dns4/".concat(aanNetworkNode.aanNodeValue.nodeHostname()).concat("/tcp/".concat(aanNetworkNode.aanNodeValue.nodePort()).concat("/p2p/".concat(peerId.toString()))));
            this.libp2pBinding.dial(this.libp2pHost, add).getController().get();
        } catch (Exception e) {
            log.info("Error al tratar de conectarse a {} en {}:{} {}", aanNetworkNode.aanNodeValue.nodeId(), aanNetworkNode.aanNodeValue.nodeHostname(), aanNetworkNode.aanNodeValue.nodePort(), e.getMessage());
//            log.info("Error {}", e.getMessage());
        }
    }

}
