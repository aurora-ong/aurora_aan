package ong.aurora.aan.core.network.node.command.update_status;

import ong.aurora.aan.command.AANCommand;
import ong.aurora.aan.command.Command;
import ong.aurora.aan.command.CommandProjectorQueryException;
import ong.aurora.aan.command.CommandValidationException;
import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.event.EventData;
import ong.aurora.aan.core.network.node.entity.AANNodeEntity;
import ong.aurora.aan.core.network.node.entity.AANNodeKey;
import ong.aurora.aan.core.network.node.entity.AANNodeStatus;
import ong.aurora.aan.core.network.node.entity.AANNodeValue;
import ong.aurora.aan.projector.AANProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;


public class UpdateNodeStatus implements AANCommand {

    private static final Logger logger = LoggerFactory.getLogger(UpdateNodeStatus.class);

    @Override
    public String commandName() {
        return "ann_node.update.status";
    }

    @Override
    public Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandProjectorQueryException, CommandValidationException {
        logger.info("Validando {}", this.getClass().getName());

        UpdateNodeStatusCommandData commandData = command.commandData2(UpdateNodeStatusCommandData.class);

        MaterializedEntity<AANNodeValue> aanNode = projector.queryOne(new AANNodeEntity(), new AANNodeKey(commandData.nodeId())).orElseThrow(() -> new CommandValidationException("El nodo no se pudo encontrar"));


        AANNodeValue aanNodeValue = aanNode.getEntityValue();

        AANNodeStatus updatedStatus = AANNodeStatus.valueOf(commandData.nodeStatus());

//
        Queue<EventData> eventQueue = new LinkedList<>();

        EventData eventData = EventData.updateEntity(new AANNodeEntity(), new AANNodeValue(aanNodeValue.nodeId(), aanNodeValue.nodeName(), aanNodeValue.nodeHostname(), aanNodeValue.nodeSignature(), aanNodeValue.nodePort(), updatedStatus));

        eventQueue.add(eventData);

        return eventQueue;

    }


}
