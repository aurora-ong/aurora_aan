package ong.aurora.ann.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AANNetworkHost {

    AANNetwork aanNetwork;
    BehaviorSubject<List<AANNetworkNode>> networkNodes;

    BehaviorSubject<AANNetworkHostStatusType> nodeStatus = BehaviorSubject.create(AANNetworkHostStatusType.DISCONNECTED);

    private static final Logger logger = LoggerFactory.getLogger(AANNetworkHost.class);

    public AANNetworkHost(BehaviorSubject<List<AANNetworkNode>> networkNodes, AANNetwork aanNetwork) {
        this.networkNodes = networkNodes;
        this.aanNetwork = aanNetwork;

        nodeStatus.subscribe(status -> {
            if (status == AANNetworkHostStatusType.DISCONNECTED) {

                if (!networkNodes.getValue().isEmpty()) {
                    logger.info("Estableciendo conexión con la red");
                    networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.nodeStatus.getValue() == AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetwork::establishConnection);

                    var r = networkNodes.asObservable()
                            .flatMap(aanNetworkNodes -> Observable.combineLatest(aanNetworkNodes.stream().map(AANNetworkNode::onStatusChange).toList(), args1 -> (List<AANNetworkNode>) (List<?>) Arrays.stream(args1).toList()), (aanNetworkNodes, o) -> aanNetworkNodes)
                            .first(aanNetworkNodes -> {
                                return aanNetworkNodes.stream().allMatch(aanNetworkNode -> aanNetworkNode.nodeStatus.getValue() == AANNetworkNodeStatusType.CONNECTED);
                            }).toBlocking();
                    try {
                        r.toFuture().get(5, TimeUnit.SECONDS);
                    }
                    catch (TimeoutException e) {
                        logger.info("Timeout, intentar de nuevo");
//                    throw new RuntimeException(e);
                        nodeStatus.onNext(AANNetworkHostStatusType.DISCONNECTED);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    logger.info("Esperando conexiones externas. Presiona espacio para configurar este nodo");
                }

            }
        });

//
//                .subscribe(o -> {
//            logger.info("\n======= Red actualizada =======");
//            o.forEach(aanNetworkNode -> logger.info(aanNetworkNode.toString()));
//
//            boolean rdy = o.stream().allMatch(aanNetworkNode -> aanNetworkNode.nodeStatus.getValue() == AANNetworkNodeStatusType.CONNECTED);
//
//            if (nodeStatus.getValue())
//
//
//            logger.info("==============");
//        });
    }
}
