package ong.aurora.model.v_0_0_1.command.ou.create;

import ong.aurora.commons.command.AANCommand;
import ong.aurora.commons.command.Command;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.command.CommandValidationException;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.EventData;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.model.v_0_0_1.entity.uo.OUEntity;
import ong.aurora.model.v_0_0_1.entity.uo.OUKey;
import ong.aurora.model.v_0_0_1.entity.uo.OUValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;


public class CreateOU implements AANCommand {

    private static final Logger logger = LoggerFactory.getLogger(CreateOU.class);

    @Override
    public String commandName() {
        return "ou.create";
    }



    @Override
    public Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandValidationException, CommandProjectorQueryException {
        logger.info("Validando {}", this.getClass().getName());

        CreateOUCommandData commandData = command.commandData2(CreateOUCommandData.class);

        String ouId = commandData.ouId();

        // TODO RESOLVER VALIDACIÓN ??
//        command.checkRequiredFields(List.of("ou_id", "ou_name", "ou_goal"));

        Optional<MaterializedEntity<OUValue>> OuOptional = projector.queryOne(new OUEntity(), new OUKey(ouId));

        if (OuOptional.isPresent()) {
            throw new CommandValidationException("ou.ou_already_exists");
        }

        logger.info("getOne optional: {}", OuOptional);

        List<MaterializedEntity<OUValue>> list = projector.queryAll(new OUEntity());

        logger.info("@@ getAll list");

        list.forEach(ouValueMaterializedEntity -> logger.info("entity {}", ouValueMaterializedEntity));

//        if (value.isPresent()) {
//            throw new CommandValidationException("ou_id.person_already_exists");
//        }

        Queue<EventData> eventQueue = new LinkedList<>();
        eventQueue.add(EventData.updateEntity(new OUEntity(), new OUValue(commandData.ouId(), commandData.ouName(), commandData.ouGoal())));
        return eventQueue;

    }

}
