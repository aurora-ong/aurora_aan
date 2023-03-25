package ong.aurora.commons.peer.node;

import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;

public class ANNNodeEntity extends AANEntity<AANNodeKey, AANNodeValue> {


    public ANNNodeEntity() {
        super("aan_node", AANNodeKey.class, AANNodeValue.class);
    }

    @Override
    public AANNodeKey keyFromEvent(Event event) {
        return AANNodeKey.fromEvent(event);
    }

    @Override
    public AANNodeValue valueFromEvent(Event event) {
        return AANNodeValue.fromEvent(event);
    }

    @Override
    public MaterializedEntity<AANNodeValue> materializeFromEvent(Event event) {

        return new MaterializedEntity<>(this.valueFromEvent(event), event.eventTimestamp(), event.eventTimestamp());
    }

}
