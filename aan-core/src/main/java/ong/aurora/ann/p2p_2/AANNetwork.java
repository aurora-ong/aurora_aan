package ong.aurora.ann.p2p_2;

import ong.aurora.ann.PeerController;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public interface AANNetwork {

    void startHost();

    PublishSubject<AANNetworkPeer> onNetworkConnection();

}
