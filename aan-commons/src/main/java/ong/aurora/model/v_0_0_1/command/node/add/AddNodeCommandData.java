package ong.aurora.model.v_0_0_1.command.node.add;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.command.CommandData;

public record AddNodeCommandData(@JsonProperty("node_id") String nodeId, @JsonProperty("node_name") String nodeName,
                                 @JsonProperty("node_hostname") String nodeHostname,
                                 @JsonProperty("node_port") String nodePort,
                                 @JsonProperty("node_signature") String nodeSignature) implements CommandData {
}
