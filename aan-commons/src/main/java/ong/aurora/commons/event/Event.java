package ong.aurora.commons.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.command.Command;

import java.time.Instant;
import java.util.Map;

public record Event(@JsonProperty("event_id") Long eventId,
                    @JsonProperty("event_name") String eventName,
                    @JsonProperty("event_data") Map<String, Object> eventData,
                    @JsonProperty("event_timestamp") Instant eventTimestamp,
                    @JsonProperty("event_hash") String blockHash,
                    @JsonProperty("event_command") Command eventCommand) {
}
