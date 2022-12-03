package ong.aurora.ann;


import io.libp2p.core.*;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.core.multistream.ProtocolBinding;
import io.libp2p.core.multistream.ProtocolDescriptor;
import io.libp2p.core.pubsub.PubsubPublisherApi;
import io.libp2p.core.pubsub.Topic;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.libp2p.pubsub.gossip.Gossip;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Hello world!");

        Gossip gossip = new Gossip();

        Host thisNode = new HostBuilder()
                .protocol(gossip)
//                .transport(WsTransport::new)
                .listen("/ip4/127.0.0.1/tcp/3000")
                .build();





        StreamPromise<? extends Unit> streamPromise = gossip.dial(thisNode, Multiaddr.fromString("/ip4/127.0.0.1/tcp/4000/p2p/QmZbUBpH7Dsi3tP88Jc1xvHPSdDqNTgSuq8y6hBu3pjhWt"));
        streamPromise.getController().join();
        Stream stream = streamPromise.getStream().get();

        gossip.handleConnection(stream.getConnection());

//        MSG msg = new MSG();
//
//        stream.pushHandler(msg);

        PubsubPublisherApi as = gossip.createPublisher(null, 1L);

//        gossip.

        Topic topic = new Topic("topic1");
        ByteBuf b = Unpooled.copiedBuffer("holamundo", Charset.defaultCharset());
        as.publish(b, topic);


        log.info("PeerTopics: {}", gossip.getPeerTopics().get());




//        Stream stream = streamPromise.getStream().get();

        log.info("Remote peer: {}", stream.remotePeerId());
        log.info("Protocol: {}", stream.getProtocol().get());


        stream.writeAndFlush("Hola mundo cruel");





//        gossip.dial(thisNode, Multiaddr.fromString("/ip4/127.0.0.1/tcp/4000/p2p/QmUvNe18ZZz7uXpSw8jy237hRTk2BKaSNoQy54o925W42f")).getController().get();

//        System.out.println(u.getClass().getName());

//        thisNode.addConnectionHandler(conn -> {
//            log.info("Nueva conexión remote: {} local: {} peerId {}", conn.remoteAddress(), conn.localAddress(), conn.secureSession().getRemoteId());
//            Topic topic = new Topic("topic1");
//            ByteBuf b = Unpooled.copiedBuffer("holamundo", Charset.defaultCharset());
//            as.publish(b, topic);
//        });



        thisNode.start().get();

        log.info("Escuchando en: {}", thisNode.listenAddresses());


//        thisNode.stop().get();

    }
}