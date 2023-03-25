package ong.aurora.ann.network;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface AANNetworkPeer {

    String getPeerIdentity();

    void sendMessage(Object object);

    PublishSubject<Object> onPeerMessage();

    Observable<Void> onPeerDisconected();

}
