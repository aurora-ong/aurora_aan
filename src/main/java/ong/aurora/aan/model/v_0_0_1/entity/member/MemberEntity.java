package ong.aurora.aan.model.v_0_0_1.entity.member;

import ong.aurora.aan.entity.AANEntity;
import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.event.Event;

public class MemberEntity extends AANEntity<MemberKey, MemberValue> {


    public MemberEntity() {
        super("member", MemberKey.class, MemberValue.class);
    }

    @Override
    public MemberKey keyFromEvent(Event event) {
        return MemberKey.fromEvent(event);
    }

    @Override
    public MemberValue valueFromEvent(Event event) {
        return MemberValue.fromEvent(event);
    }

    @Override
    public MaterializedEntity<MemberValue> materializeFromEvent(Event event) {

        return new MaterializedEntity<>(this.valueFromEvent(event), event.eventTimestamp(), event.eventTimestamp());
    }

}
