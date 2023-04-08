package ong.aurora.model.v_0_0_1.command.node.update_status;

import ong.aurora.commons.command.AANCommand;
import ong.aurora.commons.command.Command;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.command.CommandValidationException;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.EventData;
import ong.aurora.commons.peer.node.AANNodeEntity;
import ong.aurora.commons.peer.node.AANNodeKey;
import ong.aurora.commons.peer.node.AANNodeStatus;
import ong.aurora.commons.peer.node.AANNodeValue;
import ong.aurora.commons.projector.AANProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Optional;
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
