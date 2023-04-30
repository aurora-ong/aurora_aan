package ong.aurora.aan.model.v_0_0_1.command.node.update_status;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.command.CommandData;

public record UpdateNodeStatusCommandData(@JsonProperty("node_id") String nodeId,
                                          @JsonProperty("node_status") String nodeStatus) implements CommandData {
}
