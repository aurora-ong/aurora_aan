package ong.aurora.ann.network;

import rx.subjects.PublishSubject;

import java.util.concurrent.CompletableFuture;

public interface AANNetwork {

    CompletableFuture<Void> startHost();

    PublishSubject<AANNetworkPeer> onNetworkConnection();

    void establishConnection(AANNetworkNode aanNetworkNode);

}
