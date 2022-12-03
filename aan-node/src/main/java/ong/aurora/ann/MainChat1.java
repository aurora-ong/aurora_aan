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
import java.util.concurrent.ExecutionException;


public class MainChat1 {

    private static final Logger log = LoggerFactory.getLogger(MainChat1.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Hello world!");

        Gossip gossip = new Gossip();

        Chat chat = new Chat();

        Host thisNode = new HostBuilder()
                .protocol(new Chat())
//                .transport(WsTransport::new)
                .listen("/ip4/127.0.0.1/tcp/3000")
                .build();

        thisNode.start().get();
        log.info("Escuchando en: {}", thisNode.listenAddresses());


        Chat chat1 = new Chat();
        StreamPromise<Chatter> streamPromise = (StreamPromise<Chatter>) chat1.dial(thisNode, Multiaddr.fromString("/ip4/127.0.0.1/tcp/4000/p2p/QmSzHfcV8brJXG7NqFHPmRogynT7Ka7eNyyKaMxa4tnYjh"));

        Chatter chatter = streamPromise.getController().get();

        chatter.send("Hola mundo");










//        thisNode.stop().get();

    }
}