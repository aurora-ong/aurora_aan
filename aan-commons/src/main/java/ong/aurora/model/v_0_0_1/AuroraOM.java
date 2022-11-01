package ong.aurora.model.v_0_0_1;

import ong.aurora.commons.command.AANCommand;
import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.model.AANModel;
import ong.aurora.model.v_0_0_1.command.authorization.consume.AuthorizationConsume;
import ong.aurora.model.v_0_0_1.command.authorization.create.AuthorizationCreate;
import ong.aurora.model.v_0_0_1.command.member.add.AddMember;
import ong.aurora.model.v_0_0_1.command.ou.create.CreateOU;
import ong.aurora.model.v_0_0_1.command.person.create.CreatePerson;
import ong.aurora.model.v_0_0_1.entity.authorization.AuthorizationEntity;
import ong.aurora.model.v_0_0_1.entity.member.MemberEntity;
import ong.aurora.model.v_0_0_1.entity.person.PersonEntity;
import ong.aurora.model.v_0_0_1.entity.uo.OUEntity;

import java.util.List;

public class AuroraOM implements AANModel {

    @Override
    public List<AANEntity> getModelEntities() {
        return List.of(new PersonEntity(), new OUEntity(), new MemberEntity(), new AuthorizationEntity());
    }

    @Override
    public List<AANCommand> getModelCommands() {
        return List.of(new CreatePerson(), new CreateOU(), new AddMember(), new AuthorizationCreate(), new AuthorizationConsume());
    }
}
