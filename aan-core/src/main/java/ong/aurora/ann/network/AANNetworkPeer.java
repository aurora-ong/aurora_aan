package ong.aurora.ann.network;

import rx.Observable;

public interface AANNetworkPeer {

    String getPeerIdentity();

    void sendMessage(Object object);

    Observable<Object> onPeerMessage();

    void closeConnection();
}
