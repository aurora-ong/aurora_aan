package ong.aurora.ann.fsm;

import ong.aurora.ann.AANConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.EventHeader;
import org.springframework.statemachine.annotation.OnStateEntry;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@WithStateMachine
public class Initial {

    @Autowired
    private StateMachine<AANState, AANEvent> stateMachine;

    private static final Logger log = LoggerFactory.getLogger(Initial.class);

    //    @OnTransition(source = "INICIAL", target = "CONFIG_LOADING")
    @OnStateEntry(target = "CONFIG_LOADING")
    public void fromS1ToS22() throws Exception {
        log.info("Iniciando AAN Node");

        String filePath = "asds";

        log.info("Cargando configuración desde {}", filePath);

        AANConfig aanConfig = AANConfig.fromEnviroment();

        Message<AANEvent> message = MessageBuilder
                .withPayload(AANEvent.CONFIG_OK)
                .setHeader("aanConfig", aanConfig)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();


    }

    @OnStateEntry(source = "CONFIG_LOADING", target = "CONFIG_START")
    public void startBlockchain() throws Exception {
        log.info("Introduce el identificador del nodo: ");
        String name;
        do {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            // Reading data using readLine
            name = reader.readLine();

            // Printing the read line
            log.info("Echo: {}", name);
        } while (!name.equals("exit"));


    }

    @OnStateEntry(source = "CONFIG_LOADING", target = "BLOCKCHAIN_LOADING")
    public void startBlockchain2(@EventHeader("aanConfig") AANConfig aanConfig) throws Exception {
        log.info("Cargando blockchain desde {}", "bbb".concat(aanConfig.nodeId));


    }

}
