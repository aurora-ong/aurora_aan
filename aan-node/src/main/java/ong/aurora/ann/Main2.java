package ong.aurora.ann;


import io.libp2p.core.Connection;
import io.libp2p.core.Host;
import io.libp2p.core.P2PChannel;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.core.dsl.SecureChannelsBuilder;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.core.pubsub.PubsubSubscription;
import io.libp2p.core.pubsub.Topic;
import io.libp2p.core.security.SecureChannel;
import io.libp2p.protocol.Ping;
import io.libp2p.protocol.PingController;
import io.libp2p.pubsub.gossip.Gossip;
import io.libp2p.transport.implementation.ConnectionBuilder;
import io.libp2p.transport.ws.WsTransport;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main2 {

    private static final Logger log = LoggerFactory.getLogger(Main2.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Hello world!");

//        NetworkManager a = NetworkManager.ConfigMode();

        Gossip gossip = new Gossip();

        Host thisNode = new HostBuilder()
                .protocol(gossip)
//                .transport(WsTransport::new)
                .listen("/ip4/127.0.0.1/tcp/4000")

//                .secureChannel(privKey -> {
//                    SecureChannel secureChannel = new SecureChannel() {
//                    }
//                    return ;
//                })
                .build();





        Topic topic = new Topic("topic1");

        thisNode.addConnectionHandler(conn -> {
            log.info("Nueva conexión remote: {} local: {} peerId {}", conn.remoteAddress(), conn.localAddress(), conn.secureSession().getRemoteId());
//            try {
////                gossip.dial(thisNode, conn.secureSession().getRemoteId(), conn.localAddress()).getStream().get();
//            } catch (Exception e) {
//                log.error("Error acá {}", e);
//            }
            gossip.handleConnection(conn);

            log.info("Gossip score {}", gossip.getGossipScore(conn.secureSession().getRemoteId()));


            log.info("Peer Topics {}", gossip.getPeerTopics().join());

        });





//        thisNode.addProtocolHandler(gossip);

        PubsubSubscription pubsubSubscription = gossip.subscribe(messageApi -> {
            System.out.print("Suscribe received: " + messageApi.getData().toString());
        });

        thisNode.start().get();
        log.info("Escuchando en: {}", thisNode.listenAddresses());

//        thisNode.stop().get();

    }
}