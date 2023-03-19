package ong.aurora.ann.p2p_2;

import rx.subjects.PublishSubject;

public interface AANNetworkPeer {

    String getPeerIdentity();

    void sendMessage(Object object);

    PublishSubject<Object> onPeerMessage();

    PublishSubject<Void> onPeerDisconected();

}
