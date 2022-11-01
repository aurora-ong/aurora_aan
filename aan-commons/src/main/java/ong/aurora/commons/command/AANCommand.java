package ong.aurora.commons.command;

import ong.aurora.commons.event.EventData;
import ong.aurora.commons.projector.AANProjector;

import java.util.Queue;

public interface AANCommand {

    String commandName();

    Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandProjectorQueryException, CommandValidationException;

}
