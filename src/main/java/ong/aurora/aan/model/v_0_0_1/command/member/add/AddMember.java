package ong.aurora.aan.model.v_0_0_1.command.member.add;

import ong.aurora.aan.command.*;
import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.event.EventData;
import ong.aurora.aan.model.v_0_0_1.command.authorization.Authorizable;
import ong.aurora.aan.model.v_0_0_1.command.authorization.create.AuthorizationCreateCommandData;
import ong.aurora.aan.model.v_0_0_1.entity.member.MemberEntity;
import ong.aurora.aan.model.v_0_0_1.entity.member.MemberKey;
import ong.aurora.aan.model.v_0_0_1.entity.member.MemberStatus;
import ong.aurora.aan.model.v_0_0_1.entity.member.MemberValue;
import ong.aurora.aan.model.v_0_0_1.entity.person.PersonEntity;
import ong.aurora.aan.model.v_0_0_1.entity.person.PersonKey;
import ong.aurora.aan.model.v_0_0_1.entity.person.PersonValue;
import ong.aurora.aan.model.v_0_0_1.entity.uo.OUEntity;
import ong.aurora.aan.model.v_0_0_1.entity.uo.OUKey;
import ong.aurora.aan.model.v_0_0_1.entity.uo.OUValue;
import ong.aurora.aan.projector.AANProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;


public class AddMember implements AANCommand, Authorizable {

    private static final Logger logger = LoggerFactory.getLogger(AddMember.class);

    @Override
    public String commandName() {
        return "member.add";
    }

    @Override
    public Queue<EventData> applyCommand(Command command, AANProjector projector) throws CommandProjectorQueryException, CommandValidationException {
        logger.info("Validando {}", this.getClass().getName());

        AddMemberCommandData commandData = command.commandData2(AddMemberCommandData.class);

        String ouId = commandData.ouId();
        String personId = commandData.personId();

        Optional<MaterializedEntity<PersonValue>> person = projector.queryOne(new PersonEntity(), new PersonKey(personId));

        Optional<MaterializedEntity<OUValue>> ou = projector.queryOne(new OUEntity(), new OUKey(ouId));

        Optional<MaterializedEntity<MemberValue>> member = projector.queryOne(new MemberEntity(), new MemberKey(ouId, personId));

        logger.info("getOne person: {}", person);
        logger.info("getOne ou: {}", ou);
        logger.info("getOne member: {}", member);

        if (person.isEmpty() || ou.isEmpty()) {
            throw new CommandValidationException("invalid_state");
        }

        Queue<EventData> eventQueue = new LinkedList<>();

        eventQueue.add(EventData.updateEntity(new MemberEntity(), new MemberValue(commandData.ouId(), commandData.personId(), MemberStatus.JUNIOR)));
        return eventQueue;

    }

    @Override
    public CommandData fromAuthorization(Command command, AuthorizationCreateCommandData authorizationData) {
        AddMemberCommandData commandData = command.commandData2(AddMemberCommandData.class);
        logger.info("Aplicando autorización a datos del comando {}", command.commandName());
        logger.info("Command Data {}", commandData);
        logger.info("Authorization Data {}", authorizationData);
        AddMemberCommandData updatedCommandData = new AddMemberCommandData(authorizationData.ouEndId(), commandData.personId());
        logger.info("Resultado {}", updatedCommandData);

        return updatedCommandData;
    }
}
