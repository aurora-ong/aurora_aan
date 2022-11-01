package ong.aurora.model.v_0_0_1.entity.member;

import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;

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
