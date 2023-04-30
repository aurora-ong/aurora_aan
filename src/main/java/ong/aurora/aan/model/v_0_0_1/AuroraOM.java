package ong.aurora.aan.model.v_0_0_1;

import ong.aurora.aan.command.AANCommand;
import ong.aurora.aan.entity.AANEntity;
import ong.aurora.aan.model.AANModel;
import ong.aurora.aan.model.v_0_0_1.command.authorization.consume.AuthorizationConsume;
import ong.aurora.aan.model.v_0_0_1.command.authorization.create.AuthorizationCreate;
import ong.aurora.aan.model.v_0_0_1.command.init.InitCommand;
import ong.aurora.aan.model.v_0_0_1.command.member.add.AddMember;
import ong.aurora.aan.model.v_0_0_1.command.node.add.AddNode;
import ong.aurora.aan.model.v_0_0_1.command.node.update_status.UpdateNodeStatus;
import ong.aurora.aan.model.v_0_0_1.command.ou.create.CreateOU;
import ong.aurora.aan.model.v_0_0_1.command.person.create.CreatePerson;
import ong.aurora.aan.model.v_0_0_1.entity.authorization.AuthorizationEntity;
import ong.aurora.aan.model.v_0_0_1.entity.member.MemberEntity;
import ong.aurora.aan.model.v_0_0_1.entity.person.PersonEntity;
import ong.aurora.aan.model.v_0_0_1.entity.uo.OUEntity;

import java.util.List;

public class AuroraOM implements AANModel {

    @Override
    public List<AANEntity> getModelEntities() {
        return List.of(new PersonEntity(), new OUEntity(), new MemberEntity(), new AuthorizationEntity());
    }

    @Override
    public List<AANCommand> getModelCommands() {
        return List.of(new CreatePerson(), new CreateOU(), new AddMember(), new AuthorizationCreate(), new AuthorizationConsume(), new AddNode(), new UpdateNodeStatus());
    }

    @Override
    public AANCommand getInitialCommand() {
        return new InitCommand();
    }
}
