package ong.aurora.ann.fsm;

import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.serialization.AANSerializer;
import ong.aurora.commons.serialization.jackson.AANJacksonSerializer;
import ong.aurora.model.v_0_0_1.AuroraOM;
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

        AANSerializer aanSerializer = new AANJacksonSerializer();

        AANModel aanModel = new AuroraOM();

//        log.info("ola mundo");
//        stateMachine.sendEvent(AANEvent.CONFIG_LOADED);
        Message<AANEvent> message = MessageBuilder
                .withPayload(AANEvent.APP_STARTED)
                .setHeader("aanSerializer", aanSerializer)
                .setHeader("aanModel", aanModel)
                .build();

        stateMachine.sendEvents(Flux.just(message)).subscribe();
//
//        Message<AANEvent> message2 = MessageBuilder.withPayload(AANEvent.COMPLETED_2).build();
//        stateMachine.sendEvents(Flux.just(message2)).subscribe();
    }
}