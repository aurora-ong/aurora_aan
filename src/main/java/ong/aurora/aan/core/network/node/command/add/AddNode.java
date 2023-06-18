package ong.aurora.aan.core.network.node.command.add;

import ong.aurora.aan.command.AANCommand;
import ong.aurora.aan.command.Command;
import ong.aurora.aan.command.CommandProjectorQueryException;
import ong.aurora.aan.command.CommandValidationException;
import ong.aurora.aan.event.EventData;
import ong.aurora.aan.core.network.node.entity.AANNodeEntity;
import ong.aurora.aan.core.network.node.entity.AANNodeStatus;
import ong.aurora.aan.core.network.node.entity.AANNodeValue;
import ong.aurora.aan.projector.AANProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;


public class AddNode implements AANCommand {

    private static final Logger logger = LoggerFactory.getLogger(AddNode.class);

    @Override
    public String commandName() {
        return "ann_node.add";
    }

    @Override
    public Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandProjectorQueryException, CommandValidationException {
        logger.info("Validando {}", this.getClass().getName());

        AddNodeCommandData commandData = command.commandData2(AddNodeCommandData.class);

        logger.info("Nuevo nodo: {}", commandData);

        Queue<EventData> eventQueue = new LinkedList<>();


        try {

            EventData eventData = EventData.updateEntity(new AANNodeEntity(), new AANNodeValue(commandData.nodeId(), commandData.nodeName(), commandData.nodeHostname(), commandData.nodeSignature(), commandData.nodePort(), AANNodeStatus.ACTIVE));
            eventQueue.add(eventData);
            return eventQueue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


}
