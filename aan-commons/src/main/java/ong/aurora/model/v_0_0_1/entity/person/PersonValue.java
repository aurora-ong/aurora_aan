package ong.aurora.model.v_0_0_1.entity.person;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.event.Event;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PersonValue(@JsonProperty("person_id") String personId, @JsonProperty("person_name") String personName,
                          @JsonProperty("person_lastname") String personLastname,
                          @JsonProperty("person_mail") String personMail
) implements EntityValue<PersonValue> {

    public static PersonValue fromEvent(Event event) {

        String personId = (String) event.eventData().get("person_id");
        String personName = (String) event.eventData().get("person_name");
        String personLastname = (String) event.eventData().get("person_lastname");
        String personMail = (String) event.eventData().get("person_mail");

        return new PersonValue(personId, personName, personLastname, personMail);
    }


    @Override
    public PersonValue onUpdateValue(PersonValue newValue) {
        return new PersonValue(this.personId, newValue.personName(), newValue.personLastname(), newValue.personMail());
    }
}
