package ong.aurora.ann.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
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
                .initial(AANState.INICIAL)
                .states(EnumSet.allOf(AANState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AANState, AANEvent> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(AANState.INICIAL).target(AANState.PROJECTOR).event(AANEvent.CONFIG_LOADED)
                .action(actionnn())
                .and()
                .withExternal()
                .source(AANState.PROJECTOR).target(AANState.PROCESSOR).event(AANEvent.COMPLETED_2)
                .and()
                .withExternal()
                .source(AANState.PROCESSOR).target(AANState.BLOCKCHAIN).event(AANEvent.EMPTY_BLOCKCHAIN);
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

    @Bean
    public Action<AANState, AANEvent> actionnn() {
        return context -> {
            // do something
            log.info("Ejecutando acción {} {}", context.getSource().getId(), context.getTarget().getId());
        };
    }
}