package ong.aurora.model.v_0_0_1.entity.person;

import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;

public class PersonEntity extends AANEntity<PersonKey, PersonValue> {


    public PersonEntity() {
        super("person", PersonKey.class, PersonValue.class);
    }

    @Override
    public PersonKey keyFromEvent(Event event) {
        return PersonKey.fromEvent(event);
    }

    @Override
    public PersonValue valueFromEvent(Event event) {
        return PersonValue.fromEvent(event);
    }

    @Override
    public MaterializedEntity<PersonValue> materializeFromEvent(Event event) {
        return new MaterializedEntity<>(PersonValue.fromEvent(event), event.eventTimestamp(), event.eventTimestamp());
    }

}
