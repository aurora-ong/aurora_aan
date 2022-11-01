package ong.aurora.model.v_0_0_1.command.authorization.create;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record AuthorizationCreateCommandData(
                                                     @JsonProperty("authorization_title") String authorizationTitle,
                                                     @JsonProperty("authorization_description") String authorizationDescription,
                                                     @JsonProperty("ou_origin_id") String ouOriginId,
                                                     @JsonProperty("ou_end_id") String ouEndId,
//                                                     @JsonProperty("owner_id") String ownerId, // TODO: DEBE RESOLVERSE EN OTRA CAPA
                                                     @JsonProperty("command_name") String commandName,
                                                     @JsonProperty("command_data") Map<String, Object> commandData) {
}
