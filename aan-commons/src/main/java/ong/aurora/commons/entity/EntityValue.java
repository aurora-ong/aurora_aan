package ong.aurora.commons.entity;

public interface EntityValue<V> {

    V onUpdateValue(V newValue);
}
