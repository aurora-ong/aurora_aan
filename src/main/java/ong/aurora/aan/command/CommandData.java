package ong.aurora.aan.command;

import ong.aurora.aan.serialization.JacksonSerializer;

import java.util.Map;

public interface CommandData {

    default Map<String, Object> toMap() {
        return JacksonSerializer.toMap(this);
    }

}
