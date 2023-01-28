package ong.aurora.ann.p2p.msg;

public record P2PMessage2(Class messageType, String encodedMessage) {

    @Override
    public String toString() {
        return "P2PMessage{" +
                "messageType=" + messageType +
                ", encodedMessage='" + encodedMessage + '\'' +
                '}';
    }
}
