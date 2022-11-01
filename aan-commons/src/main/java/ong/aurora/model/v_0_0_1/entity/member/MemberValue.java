package ong.aurora.model.v_0_0_1.entity.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.event.Event;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MemberValue(@JsonProperty("ou_id") String ouId, @JsonProperty("person_id") String personId,
                          @JsonProperty("member_status") MemberStatus memberStatus) implements EntityValue<MemberValue> {

    public static MemberValue fromEvent(Event event) {


        String ouId = (String) event.eventData().get("ou_id");
        String personId = (String) event.eventData().get("person_id");
        MemberStatus memberStatus = MemberStatus.valueOf((String) event.eventData().get("member_status"));

        return new MemberValue(ouId, personId, memberStatus);
    }


    @Override
    public MemberValue onUpdateValue(MemberValue newValue) {
        return newValue;
    }
}
