package ong.aurora.model.v_0_0_1.entity.uo;

import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;

public class OUEntity extends AANEntity<OUKey, OUValue> {


    public OUEntity() {
        super("ou", OUKey.class, OUValue.class);
    }

    @Override
    public OUKey keyFromEvent(Event event) {
        return OUKey.fromEvent(event);
    }

    @Override
    public OUValue valueFromEvent(Event event) {
        return OUValue.fromEvent(event);
    }

    @Override
    public MaterializedEntity<OUValue> materializeFromEvent(Event event) {

        return new MaterializedEntity<>(this.valueFromEvent(event), event.eventTimestamp(), event.eventTimestamp());
    }

}
