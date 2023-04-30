package ong.aurora.aan.command;

import ong.aurora.aan.event.EventData;
import ong.aurora.aan.projector.AANProjector;

import java.util.Queue;

public interface AANCommand {

    String commandName();

    Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandProjectorQueryException, CommandValidationException;

}
