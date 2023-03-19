package ong.aurora.ann.network;

import rx.subjects.PublishSubject;

public interface AANNetwork {

    void startHost();

    PublishSubject<AANNetworkPeer> onNetworkConnection();

}
