package ong.aurora.ann;

public class Chat extends ChatBinding {

    public Chat() {
        super(new ChatProtocol(100000000L, 1000000000L));
    }
}
