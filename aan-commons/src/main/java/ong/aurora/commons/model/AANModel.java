package ong.aurora.commons.model;

import ong.aurora.commons.command.AANCommand;
import ong.aurora.commons.entity.AANEntity;

import java.util.List;

public interface AANModel {

    List<AANEntity> getModelEntities();

    List<AANCommand> getModelCommands();

    AANCommand getInitialCommand();
}
