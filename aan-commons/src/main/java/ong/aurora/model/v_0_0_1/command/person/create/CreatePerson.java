package ong.aurora.model.v_0_0_1.command.person.create;

import ong.aurora.commons.command.AANCommand;
import ong.aurora.commons.command.Command;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.command.CommandValidationException;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.EventData;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.model.v_0_0_1.entity.person.PersonEntity;
import ong.aurora.model.v_0_0_1.entity.person.PersonKey;
import ong.aurora.model.v_0_0_1.entity.person.PersonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;


public class CreatePerson implements AANCommand {

    private static final Logger logger = LoggerFactory.getLogger(CreatePerson.class);

    @Override
    public String commandName() {
        return "person.create";
    }

    public Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandValidationException, CommandProjectorQueryException {
        logger.info("Validando {}", this.getClass().getName());

        CreatePersonCommandData commandData = command.commandData2(CreatePersonCommandData.class);

        logger.info(commandData.toString());


//        String ouId = command.getDataString("person_id");
//        command.checkRequiredFields(List.of("person_id", "person_name", "person_lastname"));


        Optional<MaterializedEntity<PersonValue>> value = projector.queryOne(new PersonEntity(), new PersonKey(commandData.personId()));

        logger.info("getOne optional: {}", value);


//        if (value.isPresent()) {
//            throw new CommandValidationException("person.person_already_exists");
//        }

        Queue<EventData> eventQueue = new LinkedList<>();

        eventQueue.add(EventData.updateEntity(new PersonEntity(), new PersonValue(commandData.personId(), commandData.personName(), commandData.personLastname(), commandData.personMail())));
        return eventQueue;

    }

}
