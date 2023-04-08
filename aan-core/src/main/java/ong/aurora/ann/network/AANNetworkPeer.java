package ong.aurora.ann.network;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface AANNetworkPeer {

    String getPeerIdentity();

    void sendMessage(Object object);

    Observable<Object> onPeerMessage();

    Observable<Void> onPeerDisconected();

    void closeConnection();
}
