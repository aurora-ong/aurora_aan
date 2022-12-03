package ong.aurora.commons.peer.node;

import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;

public class ANNNodeEntity extends AANEntity<AANNodeKey, ANNNodeValue> {


    public ANNNodeEntity() {
        super("aan_node", AANNodeKey.class, ANNNodeValue.class);
    }

    @Override
    public AANNodeKey keyFromEvent(Event event) {
        return AANNodeKey.fromEvent(event);
    }

    @Override
    public ANNNodeValue valueFromEvent(Event event) {
        return ANNNodeValue.fromEvent(event);
    }

    @Override
    public MaterializedEntity<ANNNodeValue> materializeFromEvent(Event event) {

        return new MaterializedEntity<>(this.valueFromEvent(event), event.eventTimestamp(), event.eventTimestamp());
    }

}
