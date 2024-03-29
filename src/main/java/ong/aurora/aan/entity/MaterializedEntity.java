package ong.aurora.aan.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class MaterializedEntity<V extends EntityValue<V>> {

    @JsonProperty("entity_value")
    V entityValue;

    @JsonProperty("created_at")
    Instant createdAt;

    @JsonProperty("updated_at")
    Instant updatedAt;


    public MaterializedEntity(@JsonProperty("entity_value") V entityValue, @JsonProperty("created_at") Instant createdAt, @JsonProperty("updated_at") Instant updatedAt) {
        this.entityValue = entityValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public MaterializedEntity<V> onUpdateValue(MaterializedEntity<V> newValue) {
        this.entityValue = this.entityValue.onUpdateValue(newValue.entityValue);
        this.updatedAt = newValue.createdAt;
        return this;
    }

    public V getEntityValue() {
        return entityValue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "MaterializedEntity{" +
                "entityValue=" + entityValue +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
