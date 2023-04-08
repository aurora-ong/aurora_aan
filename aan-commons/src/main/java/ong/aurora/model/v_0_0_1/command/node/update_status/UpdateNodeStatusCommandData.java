package ong.aurora.model.v_0_0_1.command.node.update_status;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.command.CommandData;

public record UpdateNodeStatusCommandData(@JsonProperty("node_id") String nodeId,
                                          @JsonProperty("node_status") String nodeStatus) implements CommandData {
}
