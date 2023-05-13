package ong.aurora.aan.core.command_pool;

import ong.aurora.aan.command.Command;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.ArrayList;
import java.util.List;

public class CommandPool {


    BehaviorSubject<List<CommandIntent>> commandIntentList = BehaviorSubject.create(new ArrayList<>());



    public void addCommand(Command command) {

        commandIntentList.getValue().add(new CommandIntent(command));
        commandIntentList.onNext(commandIntentList.getValue());

    }

    public Observable<List<CommandIntent>> pollCommands() {
        return this.commandIntentList;
    }



}
