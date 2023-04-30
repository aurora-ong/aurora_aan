package ong.aurora.aan.model;

import ong.aurora.aan.command.AANCommand;
import ong.aurora.aan.entity.AANEntity;

import java.util.List;

public interface AANModel {

    List<AANEntity> getModelEntities();

    List<AANCommand> getModelCommands();

    AANCommand getInitialCommand();
}
