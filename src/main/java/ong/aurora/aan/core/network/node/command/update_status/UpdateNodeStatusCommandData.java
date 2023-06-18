package ong.aurora.aan.core.network.node.command.update_status;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.command.CommandData;

public record UpdateNodeStatusCommandData(@JsonProperty("node_id") String nodeId,
                                          @JsonProperty("node_status") String nodeStatus) implements CommandData {
}
