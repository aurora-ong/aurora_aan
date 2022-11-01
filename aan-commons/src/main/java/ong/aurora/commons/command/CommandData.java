package ong.aurora.commons.command;

import ong.aurora.commons.serialization.JacksonSerializer;

import java.util.Map;

public interface CommandData {

    default Map<String, Object> toMap() {
        return JacksonSerializer.toMap(this);
    }

}
