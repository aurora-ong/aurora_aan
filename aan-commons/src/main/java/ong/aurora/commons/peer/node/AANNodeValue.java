package ong.aurora.commons.peer.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.event.Event;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AANNodeValue(
        @JsonProperty("node_id") String nodeId,
        @JsonProperty("node_name") String nodeName,
        @JsonProperty("node_hostname") String nodeHostname,
        @JsonProperty("node_port") String nodePort,
        @JsonProperty("node_signature") String nodeSignature,
        @JsonProperty("node_status") ANNNodeStatus nodeStatus

) implements EntityValue<AANNodeValue> {

    public static AANNodeValue fromEvent(Event event) {

        final ObjectMapper mapper = new ObjectMapper();
        AANNodeValue data = mapper.convertValue(event.eventData(), AANNodeValue.class);
        return data;
    }


    @Override
    public AANNodeValue onUpdateValue(AANNodeValue newValue) {
        return newValue;
    }
}
