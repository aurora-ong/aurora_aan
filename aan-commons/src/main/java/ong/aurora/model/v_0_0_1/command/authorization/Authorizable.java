package ong.aurora.model.v_0_0_1.command.authorization;

import ong.aurora.commons.command.Command;
import ong.aurora.commons.command.CommandData;
import ong.aurora.model.v_0_0_1.command.authorization.create.AuthorizationCreateCommandData;

public interface Authorizable {


    CommandData fromAuthorization(Command command, AuthorizationCreateCommandData authorizationData);


}
