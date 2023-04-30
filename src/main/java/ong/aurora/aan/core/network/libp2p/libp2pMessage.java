package ong.aurora.aan.core.network.libp2p;

public record libp2pMessage(Class messageType, String encodedMessage) {

    @Override
    public String toString() {
        return "P2PMessage{" +
                "messageType=" + messageType +
                ", encodedMessage='" + encodedMessage + '\'' +
                '}';
    }
}
