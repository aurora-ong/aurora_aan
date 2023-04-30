package ong.aurora.aan.projector.rdb_projector;

import ong.aurora.aan.entity.MaterializedEntity;
import ong.aurora.aan.entity.EntityValue;
import ong.aurora.aan.event.Event;

import java.util.List;

public record RDBEntityStore<V extends EntityValue<V>>(List<Event> eventList, MaterializedEntity<V> materializedEntity) {


}
