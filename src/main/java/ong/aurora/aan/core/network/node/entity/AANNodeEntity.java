package ong.aurora.aan.core.network.node.entity;

import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.entity.AANEntity;
import ong.aurora.aan.event.Event;

public class AANNodeEntity extends AANEntity<AANNodeKey, AANNodeValue> {


    public AANNodeEntity() {
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
