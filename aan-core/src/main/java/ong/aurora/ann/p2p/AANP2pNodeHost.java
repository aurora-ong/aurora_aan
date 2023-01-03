package ong.aurora.ann.p2p;

import io.libp2p.core.ConnectionHandler;
import io.libp2p.core.Host;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.Builder;
import io.libp2p.crypto.keys.RsaPrivateKey;
import ong.aurora.ann.ChatBinding;
import ong.aurora.ann.ChatProtocol;
import ong.aurora.ann.identity.ANNNodeIdentity;
import ong.aurora.commons.peer.node.ANNNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.functions.FuncN;
import rx.subjects.BehaviorSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AANP2pNodeHost {

    Host p2pHost;

    BehaviorSubject<List<AANP2PNodePeer>> networkPeers = BehaviorSubject.create(List.of());

    Subscription nodeStatusSubscription;

    private static final Logger log = LoggerFactory.getLogger(AANP2pNodeHost.class);

    public AANP2pNodeHost(ANNNodeIdentity thisNodeIdentity, ANNNodeValue thisNodeValue, BehaviorSubject<List<ANNNodeValue>> projectorNodes) {
        AANProtocol protocol = new AANProtocol(networkPeers);
        AANBinding a = new AANBinding(protocol);

        this.p2pHost = new Builder().protocols(protocolBindings -> {
                    protocolBindings.add(a);
                    return null;
                }).network(networkConfigBuilder -> {
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
                // TODO CERRAR CONEXIÓN
                log.info("Cerrando conexión {}", nodeValue.toString());
            });

            List<AANP2PNodePeer> updatedPeers = annNodeValues.stream().map(nodeValue -> new AANP2PNodePeer(this.p2pHost, nodeValue)).toList();
            networkPeers.onNext(updatedPeers);


            if (nodeStatusSubscription != null && !nodeStatusSubscription.isUnsubscribed()) {
                log.info("Cerrando subscripción: nodeStatusSubscription");
                nodeStatusSubscription.unsubscribe();
            }

            nodeStatusSubscription = Observable.combineLatest(updatedPeers.stream().map(AANP2PNodePeer -> AANP2PNodePeer.peerStatus.asObservable()).toList(), args -> {

                List<P2PPeerStatus> peerStatusList = (List<P2PPeerStatus>) (List<?>) Arrays.stream(args).collect(Collectors.toList());

                log.info("Estado nodos actualizados \n{}", peerStatusList);

                return peerStatusList;
            }).subscribe(o -> {

                boolean allReady = o.stream().allMatch(p2PPeerStatus -> p2PPeerStatus.currentStatus == P2PPeerStatusType.READY);

                if (allReady) {
                    log.info("++ Conexión con la red establecida");
                    this.broadCast();
                }

//                if (o.stream().allMatch(aanp2PNodePeer -> aanp2PNodePeer.))

            });

            updatedPeers.forEach(aanp2PNodePeer -> aanp2PNodePeer.connect(a));

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


        return p2pHost.start().thenAccept(unused -> log.info("Escuchando en: {}", p2pHost.listenAddresses()));

    }

    public void broadCast() {
        this.networkPeers.getValue().forEach(aanp2PNodePeer -> aanp2PNodePeer.chatController.send("Test probando"));
    }

    public CompletableFuture<Void> dialNodes(List<ANNNodeValue> nodeValueList) {


//        this.hostPeers = nodeValueList.stream().map(nodeValue -> {
//            return new AANP2PNodePeer(this.p2pHost, nodeValue);
//
//
//        }).toList();

//        Observable.combineLatest(this.hostPeers.stream().map(p2PPeer -> p2PPeer.peerStatus.asObservable()).iterator(), new FuncN() {
//            @Override
//            public Object call(Object... args) {
//                return null;
//            }
//        });


        return CompletableFuture.completedFuture(null);
    }


}
