package ong.aurora.aan.core.network;

import kotlin.Pair;
import ong.aurora.aan.blockchain.AANBlockchain;
import ong.aurora.aan.command.Command;
import ong.aurora.aan.core.AANProcessor;
import ong.aurora.aan.core.command_pool.CommandIntent;
import ong.aurora.aan.core.command_pool.CommandStatus;
import ong.aurora.aan.core.command_pool.CreateCommandData;
import ong.aurora.aan.core.network.message.SendCommandMessage;
import ong.aurora.aan.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AANNetworkHost {

    AANNetwork aanNetwork;
    BehaviorSubject<List<AANNetworkNode>> networkNodes;

    BehaviorSubject<List<CommandIntent>> commandList = BehaviorSubject.create(new ArrayList<>());

    private static final Logger logger = LoggerFactory.getLogger(AANNetworkHost.class);

    AANBlockchain aanBlockchain;

    AANProcessor aanProcessor;

    Scheduler networkStatusScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    BehaviorSubject<Boolean> networkLocked = BehaviorSubject.create(false);

    public AANNetworkHost(BehaviorSubject<List<AANNetworkNode>> networkNodes, AANNetwork aanNetwork, AANBlockchain aanBlockchain, Scheduler schedulerExecutor, AANProcessor aanProcessor) {
        this.networkNodes = networkNodes;
        this.aanNetwork = aanNetwork;
        this.aanBlockchain = aanBlockchain;
        this.aanProcessor = aanProcessor;

        // RECONNECTION LISTENER
        networkStatusListener()
                .map(aanNetworkNodes -> aanNetworkNodes.stream().anyMatch(node -> node.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED))
                .distinctUntilChanged()
                .switchMap(this::reconnectionObserver)
                .subscribeOn(networkStatusScheduler)
                .subscribe();

        // BLOCKCHAIN UPDATER
        Observable.combineLatest(networkStatusListener(), aanBlockchain.lastEventStream, Pair::new)
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::blockchainUpdater);

        // BLOCKCHAIN BALANCER
        Observable.combineLatest(networkStatusListener(), aanBlockchain.lastEventStream, Pair::new)
                .observeOn(schedulerExecutor)
                .subscribe(this::blockchainBalancer);

        // BLOCKCHAIN BALANCER RESPONSER
        this.networkBlockchainRequestListener()
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::blockchainBalancerServer);

        // NETWORK LOGGER
        networkStatusListener()
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(this::networkLogger);

        // NETWORK CONNECTED
        networkStatusListener()
                .filter(aanNetworkNodes -> aanNetworkNodes.stream().allMatch(node -> node.currentStatus() == AANNetworkNodeStatusType.CONNECTED))
                .observeOn(networkStatusScheduler)
                .subscribe(aanNetworkNodes -> {

                });

        // COMMAND POOL
//        commandPool.pollCommands()
//                .delaySubscription(networkReadyListener())
//                .subscribe(commandList -> {
//                    logger.info("Actualizando commandPool {}", commandList);
//                    commandList.forEach(commandIntent -> this.networkNodes.getValue().forEach(aanNetworkNode -> aanNetworkNode.sendCommand(commandIntent.commandData())));
//
//                });

        // COMMAND LISTENER
        this.networkCommandListener()
                .observeOn(rx.schedulers.Schedulers.io())
                .subscribe(aanNetworkNodeCommandPair -> {
                    if (commandList.getValue().stream().noneMatch(commandIntent -> commandIntent.getCommandId().equals(aanNetworkNodeCommandPair.component2().commandId()))) {
                        logger.info("[{}] Nuevo comando recibido {}", aanNetworkNodeCommandPair.component1().aanNodeValue.nodeId(), aanNetworkNodeCommandPair.component2());
                        List<CommandIntent> commandIntentList = commandList.getValue();
                        commandIntentList.add(new CommandIntent(aanNetworkNodeCommandPair.component2()));
                        commandList.onNext(commandIntentList);
                        // GOSSIP MODEL
                        this.networkBroadcast(new SendCommandMessage(aanNetworkNodeCommandPair.component2()));
                    } else {
                        logger.info("Comando ya recibido, ignorando");
                    }


                });

        // COMMAND LISTENER
        this.commandList
                .subscribe(commandList -> {
                    logger.info("======= Comandos actualizados ({}) =======", commandList.size());
                    commandList.forEach(commandIntent -> logger.info(commandIntent.toString()));
                    logger.info("=======");
                });

        // COMMAND LISTENER
        this.commandList
                .subscribeOn(networkStatusScheduler)
                .subscribe(commandList -> {

                    Optional<CommandIntent> commandIntentOptional = commandList.stream().filter(commandIntent -> commandIntent.getCommandStatus() == CommandStatus.PENDING).findFirst();

                    commandIntentOptional.ifPresent(commandIntent -> {
                        logger.info("Procesando comando {}", commandIntent.getCommandData());

                        try {
                            this.aanProcessor.process(commandIntent.getCommandData());
                            logger.info("Comando procesado {}", commandIntent.getCommandId());
                            commandIntent.setCommandStatus(CommandStatus.SUCCESS);
                            this.commandList.onNext(this.commandList.getValue());

                        } catch (Exception exception) {
                            logger.error("Error al procesar comando {}", commandIntent);
                        }
                    });

                    commandList.forEach(commandIntent -> logger.info(commandIntent.toString()));
                    logger.info("=======");
                });


    }

    Observable<List<AANNetworkNode>> networkStatusListener() {
        return networkNodes.asObservable()
                .observeOn(networkStatusScheduler)
                .switchMap(aanNetworkNodes -> {
                    if (aanNetworkNodes.isEmpty()) {
                        return Observable.never();
                    }
                    return Observable.combineLatest(aanNetworkNodes.stream().map(AANNetworkNode::onStatusChange).toList(), args1 -> (List<AANNetworkNode>) (List<?>) Arrays.stream(args1).toList());

                })
                .throttleLast(1, TimeUnit.SECONDS, networkStatusScheduler)
                .share();

    }

    Observable<Boolean> networkReadyListener() {
        return networkStatusListener()
                .withLatestFrom(this.aanBlockchain.lastEventStream, Pair::new)
                .map(pair -> {
                    List<AANNetworkNode> aanNetworkNodes = pair.getFirst();
                    long currentEventIndex = Optional.ofNullable(pair.component2()).map(Event::eventId).orElse(-1L);

                    if (!aanNetworkNodes.stream().allMatch(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED)) {
                        return false;
                    }

                    return aanNetworkNodes.stream().allMatch(aanNetworkNode -> aanNetworkNode.nodeBlockchainIndex.compareTo(currentEventIndex) == 0);

                });
    }

    Observable<Pair<AANNetworkNode, Long>> networkBlockchainRequestListener() {
        return networkNodes.asObservable().flatMap(aanNetworkNodes -> Observable.merge(aanNetworkNodes.stream().map(AANNetworkNode::onBlockchainBalancerBlockRequestStream).toList()), (aanNetworkNodes, o) -> o);
    }

    Observable<Pair<AANNetworkNode, Command>> networkCommandListener() {
        return networkNodes.asObservable().flatMap(aanNetworkNodes -> Observable.merge(aanNetworkNodes.stream().map(AANNetworkNode::onCommandRequestStream).toList()), (aanNetworkNodes, c) -> c);
    }

    private void blockchainBalancer(Pair<List<AANNetworkNode>, Event> pair) {

        if (this.networkLocked.getValue()) {
            logger.info("[blockchainBalance] Red lista. Ignorando");
            return;
        }

        List<AANNetworkNode> aanNetworkNodes = pair.component1();

        long currentEventIndex = Optional.ofNullable(pair.component2()).map(Event::eventId).orElse(-1L);

        // TODO QUIZÁS CENTRALIZAR
        boolean allNodesConnected = aanNetworkNodes.stream().allMatch(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED);


        // TODO SOLUCIONAR CASO INDEX == -1
        boolean allNodesIndex = aanNetworkNodes.stream().allMatch(aanNetworkNode -> aanNetworkNode.nodeBlockchainIndex.compareTo(currentEventIndex) == 0);

        if (allNodesConnected && allNodesIndex) {
            logger.info("[blockchainBalance] Blockchain balanceada. Red lista");
            this.networkLocked.onNext(true);
            return;
        }

        // ENCONTRAR UN NODO QUE PUEDA PROPORCIONARNOS EL SIGUIENTE BLOQUE
        Optional<AANNetworkNode> nodeOptional = aanNetworkNodes.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED).filter(aanNetworkNode -> aanNetworkNode.nodeBlockchainIndex != null).filter(aanNetworkNode -> aanNetworkNode.nodeBlockchainIndex > currentEventIndex).findFirst();

        if (nodeOptional.isPresent()) {
            logger.info("[blockchainBalance] Solicitando evento {} a {}", currentEventIndex + 1, nodeOptional.get().aanNodeValue.nodeId());
            try {
                Event a = nodeOptional.get().sendRequestBlock(currentEventIndex + 1).join();
                aanBlockchain.persistEvent(a).join();

            } catch (Exception e) {
                logger.error("Error al balancear bloque {} desde nodo {}", currentEventIndex + 1, nodeOptional.get().aanNodeValue.nodeId());
            }
        } else {
            logger.info("[blockchainBalance] Blockchain balanceada parcialmente");
        }
    }

    private Observable<Long> reconnectionObserver(boolean networkDisconnected) {

        {
            logger.info("[networkConnection] Red desconectada ? {}", networkDisconnected);

            if (!networkDisconnected) {
                logger.info("[networkConnection] Reconexión desactivada");
                return Observable.never();
            }
            logger.info("[networkConnection] Reconexión activada");

            return Observable
                    .interval(5, 15, TimeUnit.SECONDS)
                    .doOnNext(aLong -> {
                        logger.info("[networkConnection] Intentando reconectar ({} intento)", aLong + 1);
                        this.networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.DISCONNECTED).forEach(aanNetwork::establishConnection);

                    });

        }

    }

    private void blockchainUpdater(Pair<List<AANNetworkNode>, Event> pair) {
        logger.info("[blockchainUpdater] Ejecutando");
        List<AANNetworkNode> networkNodeList = pair.component1();

        Long eventId = Optional.ofNullable(pair.component2()).map(Event::eventId).orElse(-1L);

        List<AANNetworkNode> notificableNodes = networkNodeList.stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() != AANNetworkNodeStatusType.DISCONNECTED).toList();

        if (notificableNodes.isEmpty()) {
            logger.info("[blockchainUpdater] No hay nodos para enviar actualización");
            return;
        }

        // ENVIAR ACTUALIZACIONES DE BLOCKCHAIN HACÍA LA RED
        notificableNodes.forEach(aanNetworkNode -> {
            logger.info("[blockchainUpdater] Enviando actualización a {} @ {} ({})", aanNetworkNode.aanNodeValue.nodeId(), eventId);
            aanNetworkNode.sendBlockchainReport(eventId);
        });
    }

    private void blockchainBalancerServer(Pair<AANNetworkNode, Long> pair) {
        logger.info("[blockchainBalancerServer] Nodo {} ha solicitado bloque {}", pair.component1().aanNodeValue.nodeId(), pair.component2());
        try {
            Event event = aanBlockchain.eventStream().filter(e -> Objects.equals(e.eventId(), pair.component2())).findFirst().orElseThrow(() -> new Exception("Event not found"));
            pair.component1().sendBlockchainBalancerRespondBlock(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void networkLogger(List<AANNetworkNode> aanNetworkNodes) {
        logger.info("======= Red actualizada =======");
        aanNetworkNodes.forEach(aanNetworkNode -> logger.info(aanNetworkNode.toString()));
        logger.info("=======");
    }

    private boolean isNetworkReady() {
        return this.networkNodes.getValue().stream().allMatch(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED);
    }

    public String acceptCommand(CreateCommandData commandData) throws Exception {


        if (!isNetworkReady()) {
            throw new Exception("no listo");
        }
        String commandId = UUID.randomUUID().toString();

        Command command = new Command(UUID.randomUUID().toString(), Instant.now(), commandData.commandName(), commandData.commandData());


//        this.networkNodes.getValue().stream().forEach(aanNetworkNode -> aanNetworkNode.sendCommand(command));

        networkBroadcast(new SendCommandMessage(command));

        return commandId;
    }

    private void networkBroadcast(AANNetworkMessage message) {
        this.networkNodes.getValue().stream().filter(aanNetworkNode -> aanNetworkNode.currentStatus() == AANNetworkNodeStatusType.CONNECTED).forEach(aanNetworkNode -> aanNetworkNode.sendPeerMessage(message));
    }


}