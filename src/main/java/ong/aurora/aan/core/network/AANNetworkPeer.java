package ong.aurora.aan.core.network;

import rx.Observable;

public interface AANNetworkPeer {

    String getPeerIdentity();

    void sendMessage(Object object);

    Observable<Object> onPeerMessage();

    void closeConnection();
}
