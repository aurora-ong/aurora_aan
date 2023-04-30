package ong.aurora.aan.model.v_0_0_1.entity.uo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.entity.EntityValue;
import ong.aurora.aan.event.Event;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OUValue(@JsonProperty("ou_id") String ouId, @JsonProperty("ou_name") String ouName,
                      @JsonProperty("ou_goal") String ouGoal) implements EntityValue<OUValue> {

    public static OUValue fromEvent(Event event) {

        String ouId = (String) event.eventData().get("ou_id");
        String ouName = (String) event.eventData().get("ou_name");
        String ouGoal = (String) event.eventData().get("ou_goal");

        return new OUValue(ouId, ouName, ouGoal);
    }


    @Override
    public OUValue onUpdateValue(OUValue newValue) {

//        List<OUValue> OUValueList = new java.util.ArrayList<>(this.history == null ? List.of() : this.history);
//
//        OUValueList.add(new OUValue(null, this.ouName(), this.ouGoal(), this.personMail(), null, null, this.updatedAt()));
//
//        OUValue updated = new OUValue(this.memberId, newValue.ouName(), newValue.ouGoal(), newValue.personMail(), OUValueList, this.createdAt, newValue.createdAt);

        return newValue;
    }
}
