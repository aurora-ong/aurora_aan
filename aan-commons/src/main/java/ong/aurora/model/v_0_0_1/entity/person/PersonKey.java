package ong.aurora.model.v_0_0_1.entity.person;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.event.Event;

public record PersonKey(@JsonProperty("person_id") String personId) {

    static public PersonKey fromEvent(Event event) {
        String personId = (String) event.eventData().get("person_id");
        return new PersonKey(personId);
    }



}
