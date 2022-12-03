package ong.aurora.ann;


import io.libp2p.core.Host;
import io.libp2p.core.Stream;
import io.libp2p.core.StreamPromise;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.core.pubsub.PubsubPublisherApi;
import io.libp2p.core.pubsub.Topic;
import io.libp2p.pubsub.gossip.Gossip;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kotlin.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainChat1 {

    private static final Logger log = LoggerFactory.getLogger(MainChat1.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Hello world!");

        Gossip gossip = new Gossip();

        Chat chat1 = new Chat();

        String otherNodeAddr = "/ip4/127.0.0.1/tcp/4000/p2p/QmQjMSGMPN1Gh9rBmVuaAfPvVfXB6pusMgTJnY1PiaSK7U";


        Host thisNode = new HostBuilder()
                .protocol(chat1)
//                .transport(WsTransport::new)
                .listen("/ip4/127.0.0.1/tcp/3000")
                .build();

        thisNode.start().get();
        log.info("Escuchando en: {}", thisNode.listenAddresses());


//        StreamPromise<Chatter> streamPromise = (StreamPromise<Chatter>) chat1.dial(thisNode, Multiaddr.fromString("/ip4/127.0.0.1/tcp/4000/p2p/QmZYoyMLjV4tYhetNzd1uxEM26sxHLdheJL8PABF1LpBgM"));

        ChatController chatController = chat1.dial(thisNode, Multiaddr.fromString(otherNodeAddr)).getController().get();

        chatController.send("Holiwi");


//
//        streamPromise.getController().thenAccept(chatter1 -> {
//            log.info("Controller obtenido");
//            try {
//                Thread.sleep(2000);
//                chatter1.send("Hola mundo");
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        });



//        thisNode.stop().get();

    }
}