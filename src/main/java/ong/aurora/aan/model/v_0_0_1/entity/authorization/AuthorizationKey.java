package ong.aurora.aan.model.v_0_0_1.entity.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.event.Event;

public record AuthorizationKey(@JsonProperty("authorization_id") String ouId) {

    static public AuthorizationKey fromEvent(Event event) {
        String authorizationId = (String) event.eventData().get("authorization_id");
        return new AuthorizationKey(authorizationId);
    }



}
