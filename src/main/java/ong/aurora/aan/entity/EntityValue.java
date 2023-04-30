package ong.aurora.aan.entity;

public interface EntityValue<V> {

    V onUpdateValue(V newValue);
}
