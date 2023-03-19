package ong.aurora.ann.p2p;

import io.libp2p.core.Host;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.Builder;
import io.libp2p.crypto.keys.RsaPrivateKey;
import ong.aurora.ann.identity.ANNNodeIdentity;
import ong.aurora.ann.p2p.msg.BlockchainStatus;
import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.peer.node.ANNNodeValue;
import ong.aurora.commons.serialization.AANSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static ong.aurora.commons.util.Utils.maybeUnsubscribe;

public class p2pHostNode2 {

    Host libp2pHost;

    BehaviorSubject<List<p2pPeerNode2>> networkPeers = BehaviorSubject.create(List.of());

    AANBlockchain hostBlockchain;

    Subscription networkStatusSubscription;
    Subscription blockchainUpdateSubscription;

    private static final Logger log = LoggerFactory.getLogger(p2pHostNode2.class);

    public p2pHostNode2(ANNNodeIdentity thisNodeIdentity, ANNNodeValue thisNodeValue, AANSerializer annSerializer, BehaviorSubject<List<ANNNodeValue>> projectorNodes, AANBlockchain hostBlockchain) {
        this.hostBlockchain = hostBlockchain;
        AANProtocol2 protocol = new AANProtocol2(networkPeers, annSerializer);
        AANBinding2 aanBinding = new AANBinding2(protocol);

        this.libp2pHost = new Builder()
                .protocols(protocolBindings -> {
                    protocolBindings.add(aanBinding);
                    return null;
                })
                .network(networkConfigBuilder -> {
                    String add = "/dns4/".concat(thisNodeValue.nodeHostname()).concat("/tcp/".concat(thisNodeValue.nodePort()));
                    networkConfigBuilder.listen(add);
                    return null;
                })
                .identity(identityBuilder -> {
                    identityBuilder.setFactory(() -> {

                        PrivKey privKey = new RsaPrivateKey(thisNodeIdentity.privateKey, thisNodeIdentity.publicKey);

                        return privKey;
                    });
                    return null;
                })
                .build(Builder.Defaults.Standard);

        projectorNodes.asObservable().map(annNodeValues -> annNodeValues.stream().filter(nodeValue -> !Objects.equals(nodeValue.nodeId(), thisNodeValue.nodeId())).toList()).subscribe(annNodeValues -> {
            log.info("ANN Node List actualizado \n{}", annNodeValues.stream().toList());

            networkPeers.getValue().forEach(nodeValue -> {
                log.info("Cerrando conexión {}", nodeValue.toString());
                nodeValue.dispose();
            });

            List<p2pPeerNode2> updatedPeers = annNodeValues.stream().parallel().map(nodeValue -> new p2pPeerNode2(this, nodeValue)).toList();
            networkPeers.onNext(updatedPeers);

            maybeUnsubscribe(networkStatusSubscription);

            networkStatusSubscription = Observable.combineLatest(updatedPeers.stream().map(p2pPeerNode -> p2pPeerNode.peerStatus.asObservable()).toList(), args -> {

                List<p2pPeerStatus> peerStatusList = (List<p2pPeerStatus>) (List<?>) Arrays.stream(args).collect(Collectors.toList());

                log.info("Estado nodos actualizados \n{}", peerStatusList);

                return peerStatusList;
            }).subscribe(peerStatusList -> {

                boolean networkReady = peerStatusList.stream().allMatch(p2PPeerStatus -> p2PPeerStatus.currentStatus() != P2PPeerConnectionStatusType.DISCONNECTED);

                if (networkReady) {
                    log.info("++ Conexión con la red establecida");

                    if (blockchainUpdateSubscription == null || blockchainUpdateSubscription.isUnsubscribed()) {
                        blockchainUpdateSubscription = this.hostBlockchain.blockchainStream.subscribe(event -> {
                            log.info("Informando blockchain {}", this.hostBlockchain.blockchainStream.getValue());
                            this.broadcastMessage(new BlockchainStatus(Optional.ofNullable(this.hostBlockchain.blockchainStream.getValue()).map(Event::eventId).orElse(-1L)));
                        });
                    }
//
//                    if (peerStatusList.stream().allMatch(p2pPeerStatus -> p2pPeerStatus.blockchainIndex() != null)) {
//                        log.info("Network blockchain status ready");
//                        Long maxBlockchainIndex = peerStatusList.stream().max(Comparator.comparingLong(p2pPeerStatus::blockchainIndex)).orElseThrow(() -> new RuntimeException("No blockchain index")).blockchainIndex();
//
//                        log.info("maxBlockchainIndex: {}", maxBlockchainIndex);
//
//                        Long localBlockchainIndex = this.hostBlockchain.lastEvent().map(Event::eventId).orElse(-1L);
//
//                        if (localBlockchainIndex.compareTo(maxBlockchainIndex) >= 0) {
//                            log.info("++ Nodo sincronizado maxBlockchainIndex: {} localIndex: {}", maxBlockchainIndex, localBlockchainIndex);
//                        } else {
//                            log.info("!! Nodo dessincronizado maxBlockchainIndex: {} localIndex: {}", maxBlockchainIndex, localBlockchainIndex);
//                        }
//
//                    }

                } else {
                    log.info("!! Conexión con la red perdida");
                    maybeUnsubscribe(blockchainUpdateSubscription);
                }


            });

            updatedPeers.forEach(p2PPeerNode -> p2PPeerNode.localConnect(aanBinding));

        });

//        projectorNodes.asObservable().doOnEach(notification -> {
//           log.info("ANN Node List actualizado \n{}", notification.getValue());
//
//            List<P2PPeer> updatedPeers = ((Iterable<ANNNodeValue>) notification.getValue().
//
//        });

        networkPeers.asObservable().subscribe(annNodeValues -> {
            log.info("Network peers actualizado \n{}", annNodeValues.stream().toList());
        });


    }

    public CompletableFuture<Void> start() {
        return libp2pHost.start().thenAccept(unused -> log.info("Escuchando en: {}", libp2pHost.listenAddresses()));

    }

    public void broadcastMessage(Object message) {
        this.networkPeers.getValue().forEach(p2PPeerNode -> p2PPeerNode.peerController.sendMessage2(message));
    }




}
