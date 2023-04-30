package ong.aurora.aan.event;

import ong.aurora.aan.serialization.JacksonSerializer;
import ong.aurora.aan.entity.AANEntity;
import ong.aurora.aan.entity.EntityValue;

import java.util.Map;

public class EventData<K, V extends EntityValue<V>> {

    AANEntity<K, V> eventEntity;

    EventAction eventAction;

    Map<String, Object> eventData;

    private EventData(AANEntity<K, V> eventEntity, EventAction eventAction, Object eventData) {
        this.eventEntity = eventEntity;
        this.eventAction = eventAction;
        this.eventData = JacksonSerializer.toMap(eventData);
    }

    public String eventName() {
        return this.eventEntity.entityName;
    }

    public Map<String, Object> eventData() {
        return this.eventData;
    }

    public static <K, V extends EntityValue<V>> EventData updateEntity(AANEntity<K, V> eventEntity, V eventData) {
        return new EventData(eventEntity, EventAction.UPDATE, eventData);
    }

    public static <K, V extends EntityValue<V>> EventData deleteEntity(AANEntity<K, V> eventEntity, K eventData) {
        return new EventData(eventEntity, EventAction.DELETE, eventData);
    }

}
