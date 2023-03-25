package ong.aurora.model.v_0_0_1.command.node.add;

import ong.aurora.commons.command.*;
import ong.aurora.commons.event.EventData;
import ong.aurora.commons.peer.node.ANNNodeEntity;
import ong.aurora.commons.peer.node.ANNNodeStatus;
import ong.aurora.commons.peer.node.AANNodeValue;
import ong.aurora.commons.projector.AANProjector;
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

            EventData eventData = EventData.updateEntity(new ANNNodeEntity(), new AANNodeValue(commandData.nodeId(), commandData.nodeName(), commandData.nodeHostname(), commandData.nodeSignature(), commandData.nodePort(), ANNNodeStatus.ACTIVE));
            eventQueue.add(eventData);
            return eventQueue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


}
