package ong.aurora.aan.model.v_0_0_1.command.authorization.create;

import ong.aurora.aan.command.AANCommand;
import ong.aurora.aan.command.Command;
import ong.aurora.aan.command.CommandData;
import ong.aurora.aan.command.CommandValidationException;
import ong.aurora.aan.event.EventData;
import ong.aurora.aan.model.AANModel;
import ong.aurora.aan.model.v_0_0_1.AuroraOM;
import ong.aurora.aan.model.v_0_0_1.command.authorization.Authorizable;
import ong.aurora.aan.model.v_0_0_1.entity.authorization.AuthorizationEntity;
import ong.aurora.aan.model.v_0_0_1.entity.authorization.AuthorizationStatus;
import ong.aurora.aan.model.v_0_0_1.entity.authorization.AuthorizationValue;
import ong.aurora.aan.projector.AANProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;


public class AuthorizationCreate implements AANCommand {

    AANModel aanModel = new AuroraOM();

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationCreate.class);

    @Override
    public String commandName() {
        return "authorization.create";
    }

    @Override
    public Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandValidationException {
        logger.info("Validando {}", this.getClass().getName());

        AuthorizationCreateCommandData authorizationCommandData = command.commandData2(AuthorizationCreateCommandData.class);

        logger.info("CommandData: {}", authorizationCommandData);
        logger.info("CommandData2: {}", authorizationCommandData.commandData());

        AANCommand commandProcessor = this.aanModel.getModelCommands().stream().filter(aanCommand -> aanCommand.commandName().equals(authorizationCommandData.commandName())).findFirst().orElseThrow(() -> new CommandValidationException("command_processor_not_found"));

        logger.info("command processor: {}", commandProcessor);

        if (!(commandProcessor instanceof Authorizable)) {
            throw new CommandValidationException("command_not_authorizable");
        }

        Command subCommandData = new Command(UUID.randomUUID().toString(), command.commandTimestamp(), authorizationCommandData.commandName(), authorizationCommandData.commandData());
        CommandData data = ((Authorizable) commandProcessor).fromAuthorization(subCommandData, authorizationCommandData);

        Queue<EventData> eventQueue = new LinkedList<>();

        eventQueue.add(EventData.updateEntity(new AuthorizationEntity(), new AuthorizationValue(UUID.randomUUID().toString(), authorizationCommandData.authorizationTitle(), authorizationCommandData.authorizationDescription(), AuthorizationStatus.OPEN, authorizationCommandData.ouOriginId(), authorizationCommandData.ouEndId(), "owner_id", authorizationCommandData.commandName(), data.toMap())));
        return eventQueue;

    }


}
