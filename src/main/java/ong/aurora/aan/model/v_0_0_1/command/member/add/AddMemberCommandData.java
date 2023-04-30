package ong.aurora.aan.model.v_0_0_1.command.member.add;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.command.CommandData;

public record AddMemberCommandData(@JsonProperty("ou_id") String ouId, @JsonProperty("person_id") String personId) implements CommandData {
}
