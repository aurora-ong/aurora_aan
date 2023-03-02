package ong.aurora.ann.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class AANFSM
        extends EnumStateMachineConfigurerAdapter<AANState, AANEvent> {

    private static final Logger log = LoggerFactory.getLogger(CommandLineRunner.class);



    @Override
    public void configure(StateMachineConfigurationConfigurer<AANState, AANEvent> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<AANState, AANEvent> states)
            throws Exception {
        states
                .withStates()
                .initial(AANState.INITIAL)
                .states(EnumSet.allOf(AANState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AANState, AANEvent> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(AANState.INITIAL).event(AANEvent.APP_STARTED).target(AANState.CONFIG_LOADING)
                .and()
                .withExternal()
                .source(AANState.CONFIG_LOADING).event(AANEvent.CONFIG_EMPTY).target(AANState.CONFIG_INIT)
                .and()
                .withExternal()
                .source(AANState.CONFIG_LOADING).event(AANEvent.CONFIG_OK).target(AANState.BLOCKCHAIN_LOADING)
                .and()
                .withExternal()
                .source(AANState.BLOCKCHAIN_LOADING).event(AANEvent.BLOCKCHAIN_OK).target(AANState.PROJECTOR_LOADING)
                .and()
                .withExternal()
                .source(AANState.PROJECTOR_LOADING).event(AANEvent.PROJECTOR_OK).target(AANState.PROCESSOR_LOADING)
                .and()
                .withExternal()
                .source(AANState.PROCESSOR_LOADING).event(AANEvent.PROCESSOR_OK).target(AANState.NODE_LOADING)

        ;
    }

    @Bean
    public StateMachineListener<AANState, AANEvent> listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<AANState, AANEvent> from, State<AANState, AANEvent> to) {
                log.info("State change to " + to.getId());
            }
        };
    }
//
//    @Bean
//    public Action<AANState, AANEvent> actionnn() {
//        return context -> {
//            // do something
//            log.info("Ejecutando acción {} {}", context.getSource().getId(), context.getTarget().getId());
//        };
//    }
}