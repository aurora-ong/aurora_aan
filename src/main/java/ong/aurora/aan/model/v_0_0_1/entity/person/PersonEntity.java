package ong.aurora.aan.model.v_0_0_1.entity.person;

import ong.aurora.aan.entity.AANEntity;
import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.event.Event;

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
