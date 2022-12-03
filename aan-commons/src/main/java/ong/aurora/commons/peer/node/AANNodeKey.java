package ong.aurora.commons.peer.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.event.Event;

public record AANNodeKey(@JsonProperty("node_id") String nodeId) {

    static public AANNodeKey fromEvent(Event event) {
        String authorizationId = (String) event.eventData().get("node_id");
        return new AANNodeKey(authorizationId);
    }



}
