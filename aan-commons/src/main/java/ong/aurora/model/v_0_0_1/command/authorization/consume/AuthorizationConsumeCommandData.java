package ong.aurora.model.v_0_0_1.command.authorization.consume;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorizationConsumeCommandData(
        @JsonProperty("authorization_id") String authorizationId
        ) {
}
