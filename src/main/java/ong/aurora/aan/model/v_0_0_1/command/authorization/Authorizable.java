package ong.aurora.aan.model.v_0_0_1.command.authorization;

import ong.aurora.aan.command.Command;
import ong.aurora.aan.command.CommandData;
import ong.aurora.aan.model.v_0_0_1.command.authorization.create.AuthorizationCreateCommandData;

public interface Authorizable {


    CommandData fromAuthorization(Command command, AuthorizationCreateCommandData authorizationData);


}
