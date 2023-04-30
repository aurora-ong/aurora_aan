package ong.aurora.aan.model.v_0_0_1.entity.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.event.Event;

public record MemberKey(@JsonProperty("ou_id") String ouId, @JsonProperty("person_id") String personId) {

    static public MemberKey fromEvent(Event event) {
        String ouId = (String) event.eventData().get("ou_id");
        String personId = (String) event.eventData().get("person_id");
        return new MemberKey(ouId, personId);
    }



}
