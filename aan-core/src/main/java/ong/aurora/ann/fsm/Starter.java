package ong.aurora.ann.fsm;

import ong.aurora.ann.ANNCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@SpringBootApplication
public class Starter implements CommandLineRunner {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private StateMachine<AANState, AANEvent> stateMachine;

    private static final Logger log = LoggerFactory.getLogger(CommandLineRunner.class);

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("ola mundo");
        stateMachine.sendEvent(AANEvent.CONFIG_LOADED);
        Message<AANEvent> message = MessageBuilder.withPayload(AANEvent.CONFIG_LOADED).build();
        stateMachine.sendEvents(Flux.just(message)).subscribe();

        Message<AANEvent> message2 = MessageBuilder.withPayload(AANEvent.COMPLETED_2).build();
        stateMachine.sendEvents(Flux.just(message2)).subscribe();
    }
}