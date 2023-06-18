package ong.aurora.aan.core.network.node.command.add;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.command.CommandData;

public record AddNodeCommandData(@JsonProperty("node_id") String nodeId, @JsonProperty("node_name") String nodeName,
                                 @JsonProperty("node_hostname") String nodeHostname,
                                 @JsonProperty("node_port") String nodePort,
                                 @JsonProperty("node_signature") String nodeSignature) implements CommandData {
}
