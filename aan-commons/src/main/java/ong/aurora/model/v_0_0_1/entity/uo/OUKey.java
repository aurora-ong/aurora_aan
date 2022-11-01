package ong.aurora.model.v_0_0_1.entity.uo;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.event.Event;

public record OUKey(@JsonProperty("ou_id") String ouId) {

    static public OUKey fromEvent(Event event) {
        String ouId = (String) event.eventData().get("ou_id");
        return new OUKey(ouId);
    }



}
