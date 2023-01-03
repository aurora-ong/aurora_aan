package ong.aurora.model.v_0_0_1.command.authorization.consume;

import ong.aurora.commons.command.AANCommand;
import ong.aurora.commons.command.Command;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.command.CommandValidationException;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.EventData;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.model.v_0_0_1.AuroraOM;
import ong.aurora.model.v_0_0_1.entity.authorization.AuthorizationEntity;
import ong.aurora.model.v_0_0_1.entity.authorization.AuthorizationKey;
import ong.aurora.model.v_0_0_1.entity.authorization.AuthorizationStatus;
import ong.aurora.model.v_0_0_1.entity.authorization.AuthorizationValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;


public class AuthorizationConsume implements AANCommand {

    AANModel aanModel = new AuroraOM();

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationConsume.class);

    @Override
    public String commandName() {
        return "authorization.consume";
    }

    @Override
    public Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandValidationException, CommandProjectorQueryException {
        logger.info("Validando {}", this.getClass().getName());

        AuthorizationConsumeCommandData commandData = command.commandData2(AuthorizationConsumeCommandData.class);
        logger.info("CommandData: {}", commandData);

        Optional<MaterializedEntity<AuthorizationValue>> authorizationOptional = projector.queryOne(new AuthorizationEntity(), new AuthorizationKey(commandData.authorizationId()));

        if (authorizationOptional.isEmpty()) {
            throw new CommandValidationException("authorizacion_not_exists");
        }

        AuthorizationValue authorizationValue = authorizationOptional.get().getEntityValue();

        logger.info("CommandData: {}", authorizationOptional);

        AANCommand commandProcessor = this.aanModel.getModelCommands().stream().filter(aanCommand -> aanCommand.commandName().equals(authorizationValue.commandName())).findFirst().orElseThrow(() -> new CommandValidationException("command_processor_not_found"));

        logger.info("command processor: {}", commandProcessor);

//         if (!(commandProcessor instanceof Authorizable)) {
//             throw new CommandValidationException("command_not_authorizable");
//         }

        Command newCommand = new Command(command.commandId(), command.commandTimestamp(), authorizationValue.commandName(), authorizationValue.commandData());

        Queue<EventData> eventQueue = new LinkedList<>();

        try {
            logger.info("Procesando subcomando...");

            Queue<EventData> commandQueue = commandProcessor.applyCommand(newCommand, projector);
            eventQueue.add(EventData.updateEntity(new AuthorizationEntity(), new AuthorizationValue(authorizationValue.authorizationId(), authorizationValue.authorizationTitle(), authorizationValue.authorizationDescription(), AuthorizationStatus.CONSUMED, authorizationValue.ouOriginId(), authorizationValue.ouEndId(), authorizationValue.ownerId(), authorizationValue.commandName(), authorizationValue.commandData())));
            eventQueue.addAll(commandQueue);
            logger.info("Comando procesado exitosamente");
        }
        catch (CommandValidationException e) {
            logger.info("Error al procesar subcomando (validación) {}", e.toString());
            eventQueue.add(EventData.updateEntity(new AuthorizationEntity(), new AuthorizationValue(authorizationValue.authorizationId(), authorizationValue.authorizationTitle(), authorizationValue.authorizationDescription(), AuthorizationStatus.FAILED, authorizationValue.ouOriginId(), authorizationValue.ouEndId(), authorizationValue.ownerId(), authorizationValue.commandName(), authorizationValue.commandData())));
        }
        catch (Exception e) {
            logger.info("Error al procesar subcomando {}", e.toString());
            eventQueue.add(EventData.updateEntity(new AuthorizationEntity(), new AuthorizationValue(authorizationValue.authorizationId(), authorizationValue.authorizationTitle(), authorizationValue.authorizationDescription(), AuthorizationStatus.FAILED, authorizationValue.ouOriginId(), authorizationValue.ouEndId(), authorizationValue.ownerId(), authorizationValue.commandName(), authorizationValue.commandData())));
        }

        return eventQueue;

    }


}
