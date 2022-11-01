package ong.aurora.commons.entity;

import ong.aurora.commons.event.Event;

public abstract class AANEntity<K, V extends EntityValue<V>> {

    public String entityName;
    public Class<K> keyType;
    public Class<V> valueType;

    public AANEntity(String entityName, Class<K> keyType, Class<V> valueType) {
        this.entityName = entityName;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public abstract K keyFromEvent(Event event);

    public abstract V valueFromEvent(Event event);

    public abstract MaterializedEntity<V> materializeFromEvent(Event event);

}
