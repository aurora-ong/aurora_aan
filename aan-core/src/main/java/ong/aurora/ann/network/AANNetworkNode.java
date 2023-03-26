package ong.aurora.ann.network;

import ong.aurora.commons.peer.node.AANNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class AANNetworkNode {


    private static final Logger logger = LoggerFactory.getLogger(AANNetworkNode.class);
    public AANNodeValue aanNodeValue;

    BehaviorSubject<AANNetworkNodeStatusType> nodeStatus = BehaviorSubject.create(AANNetworkNodeStatusType.DISCONNECTED);

    @Override
    public String toString() {
        return "AANNetworkNode{" +
                "aanNodeValue=" + aanNodeValue.nodeId() +
                ", nodeStatus=" + nodeStatus.getValue() +
                ", peerConnection=" + peerConnection +
                '}';
    }

    public AANNetworkPeer peerConnection;


    public AANNetworkNode(AANNodeValue aanNodeValue, AANNetworkPeer peerConnection) {
        this.aanNodeValue = aanNodeValue;
        this.peerConnection = peerConnection;
    }

    public void attachConnection(AANNetworkPeer peerConnection) {
        logger.info("Conexión establecida con {} ({}:{})", this.aanNodeValue.nodeId(), this.aanNodeValue.nodeHostname(), this.aanNodeValue.nodePort());
//        if (peerConnection != null) {
//            throw new Exception("Ya se había asignado conexión");
//        }

        this.peerConnection = peerConnection;
        this.peerConnection.onPeerDisconected().subscribe(unused -> {
            this.nodeStatus.onNext(AANNetworkNodeStatusType.DISCONNECTED);
        });
        this.nodeStatus.onNext(AANNetworkNodeStatusType.CONNECTED);
    }

    public Observable<AANNetworkNode> onStatusChange() {
        return this.nodeStatus.asObservable().map(aanNetworkNodeStatusType -> this);
    }

}
