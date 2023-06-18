package ong.aurora.aan.core.network.node.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.event.Event;

public record AANNodeKey(@JsonProperty("node_id") String nodeId) {

    static public AANNodeKey fromEvent(Event event) {
        String authorizationId = (String) event.eventData().get("node_id");
        return new AANNodeKey(authorizationId);
    }



}
