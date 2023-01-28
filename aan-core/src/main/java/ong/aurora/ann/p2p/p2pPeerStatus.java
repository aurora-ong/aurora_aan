package ong.aurora.ann.p2p;

public record p2pPeerStatus(String nodeId, P2PPeerConnectionStatusType currentStatus,
                            Long blockchainIndex) {


}
