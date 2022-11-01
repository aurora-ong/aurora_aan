package ong.aurora.model.v_0_0_1.entity.authorization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.event.Event;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorizationValue(
        @JsonProperty("authorization_id") String authorizationId,
        @JsonProperty("authorization_title") String authorizationTitle,
        @JsonProperty("authorization_description") String authorizationDescription,
        @JsonProperty("authorization_status") AuthorizationStatus authorizationStatus,
        @JsonProperty("ou_origin_id") String ouOriginId,
        @JsonProperty("ou_end_id") String ouEndId,
        @JsonProperty("owner_id") String ownerId,
        @JsonProperty("command_name") String commandName,
        @JsonProperty("command_data") Map<String, Object> commandData

) implements EntityValue<AuthorizationValue> {

    public static AuthorizationValue fromEvent(Event event) {

        final ObjectMapper mapper = new ObjectMapper();
        AuthorizationValue data = mapper.convertValue(event.eventData(), AuthorizationValue.class);
        return data;

//        String ouId = (String) event.eventData().get("ou_id");
//        String ownerId = (String) event.eventData().get("person_id");
//        AuthorizationStatus authorizationStatus = AuthorizationStatus.valueOf((String) event.eventData().get("member_status"));
//
//        return new AuthorizationValue(ouId, ownerId, authorizationStatus);
    }


    @Override
    public AuthorizationValue onUpdateValue(AuthorizationValue newValue) {
        return newValue;
    }
}
