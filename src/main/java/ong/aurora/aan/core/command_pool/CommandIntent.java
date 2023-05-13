package ong.aurora.aan.core.command_pool;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.command.Command;

public class CommandIntent {

    Command commandData;
    CommandStatus commandStatus;


    public CommandIntent(Command commandData) {
        this.commandData = commandData;
        this.commandStatus = CommandStatus.PENDING;
    }

    public String getCommandId() {
        return commandData.commandId();
    }

    public Command getCommandData() {
        return commandData;
    }

    public CommandStatus getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(CommandStatus commandStatus) {
        this.commandStatus = commandStatus;
    }
}