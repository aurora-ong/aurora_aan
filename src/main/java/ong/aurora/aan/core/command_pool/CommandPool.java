package ong.aurora.aan.core.command_pool;

import ong.aurora.aan.command.Command;

import java.util.ArrayList;
import java.util.List;

public class CommandPool {


    List<CommandIntent> commandIntentList = new ArrayList<>();

    public void addCommand(Command command) {

        commandIntentList.add(new CommandIntent(command, CommandStatus.PENDING));

    }



}
