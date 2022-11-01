package ong.aurora.commons.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class TracedEntity<V> {

    @JsonProperty("entity_value")
    V entityValue;

    @JsonProperty("created_at")
    Instant createdAt;

    @JsonProperty("updated_at")
    Instant updatedAt;


    public TracedEntity(@JsonProperty("entity_value") V entityValue, @JsonProperty("created_at") Instant createdAt, @JsonProperty("updated_at") Instant updatedAt) {
        this.entityValue = entityValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public TracedEntity<V> onUpdateValue(TracedEntity<V> newValue) {
        this.entityValue = newValue.entityValue;
        this.updatedAt = newValue.createdAt;
        return this;
    }

}
