package ong.aurora.ann.p2p;

public class P2PPeerStatus {

    P2PPeerStatusType currentStatus;

    public P2PPeerStatus(P2PPeerStatusType currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public String toString() {
        return "P2PPeerStatus{" +
                "currentStatus=" + currentStatus +
                '}';
    }
}
