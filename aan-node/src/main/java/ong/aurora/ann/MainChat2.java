package ong.aurora.ann;


import io.libp2p.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainChat2 {

    private static final Logger log = LoggerFactory.getLogger(MainChat2.class);

    public static void main(String[] args) throws Exception {


        Chat chat = new Chat();

        ANNNodeIdentity annNodeIdentity = new ANNNodeIdentity("host_2");


        Host thisNode = P2PNode.test(chat, "/ip4/127.0.0.1/tcp/4000", annNodeIdentity);


        thisNode.start().get();
        log.info("Escuchando en: {}", thisNode.listenAddresses());

    }
}