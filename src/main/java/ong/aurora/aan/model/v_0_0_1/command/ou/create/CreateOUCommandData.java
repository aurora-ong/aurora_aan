package ong.aurora.aan.model.v_0_0_1.command.ou.create;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateOUCommandData(@JsonProperty("ou_id") String ouId, @JsonProperty("ou_name") String ouName,
                                  @JsonProperty("ou_goal") String ouGoal) {
}
