package ong.aurora.ann.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.statemachine.annotation.OnStateEntry;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;

@WithStateMachine
public class Test {

    private static final Logger log = LoggerFactory.getLogger(Test.class);

    @OnTransition
    public void anyTransition() {
//        log.info("TEST TEST");
    }

    @OnTransition(source = "PROJECTOR", target = "PROCESSOR")
    public void fromS1ToS2() {
        log.info("S1 TO S2");
    }

}
