package ong.aurora.ann;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chat extends ChatBinding {

    private static final Logger log = LoggerFactory.getLogger(Chat.class);



    public Chat() {
        super(new ChatProtocol(100000000L, 1000000000L));
        log.info("Chat instanciado (chatbindind -> chat protocol)");

    }
}
