package ong.aurora.commons.projector.rdb_projector;

import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.event.Event;

import java.util.List;

public record RDBEntityStore<V extends EntityValue<V>>(List<Event> eventList, MaterializedEntity<V> materializedEntity) {


}
