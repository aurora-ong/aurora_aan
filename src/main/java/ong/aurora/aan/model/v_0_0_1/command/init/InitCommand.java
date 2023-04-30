package ong.aurora.aan.model.v_0_0_1.command.init;

import ong.aurora.aan.command.AANCommand;
import ong.aurora.aan.command.Command;
import ong.aurora.aan.command.CommandProjectorQueryException;
import ong.aurora.aan.command.CommandValidationException;
import ong.aurora.aan.event.EventData;
import ong.aurora.aan.model.v_0_0_1.entity.person.PersonEntity;
import ong.aurora.aan.model.v_0_0_1.entity.person.PersonValue;
import ong.aurora.aan.projector.AANProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;


public class InitCommand implements AANCommand {

    private static final Logger logger = LoggerFactory.getLogger(InitCommand.class);

    @Override
    public String commandName() {
        return "aan.init";
    }

    public Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandValidationException, CommandProjectorQueryException {
        logger.info("Inicializando blockchain {}", this.getClass().getName());

        InitCommandData commandData = command.commandData2(InitCommandData.class);

        logger.info(commandData.toString());



        Queue<EventData> eventQueue = new LinkedList<>();

        eventQueue.add(EventData.updateEntity(new PersonEntity(), new PersonValue(UUID.randomUUID().toString(), commandData.personName(), commandData.personLastname(), commandData.personMail())));
        return eventQueue;

    }

}
