package ong.aurora.model.v_0_0_1.entity.authorization;

import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;

public class AuthorizationEntity extends AANEntity<AuthorizationKey, AuthorizationValue> {


    public AuthorizationEntity() {
        super("authorization", AuthorizationKey.class, AuthorizationValue.class);
    }

    @Override
    public AuthorizationKey keyFromEvent(Event event) {
        return AuthorizationKey.fromEvent(event);
    }

    @Override
    public AuthorizationValue valueFromEvent(Event event) {
        return AuthorizationValue.fromEvent(event);
    }

    @Override
    public MaterializedEntity<AuthorizationValue> materializeFromEvent(Event event) {

        return new MaterializedEntity<>(this.valueFromEvent(event), event.eventTimestamp(), event.eventTimestamp());
    }

}
