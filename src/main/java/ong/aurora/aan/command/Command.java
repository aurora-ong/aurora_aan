package ong.aurora.aan.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

// TODO ADD COMMAND ID
// TODO ADD COMMAND CONTEXT
public record Command(
        @JsonProperty("command_id") String commandId,
        @JsonProperty("command_timestamp") Instant commandTimestamp,
        @JsonProperty("command_name") String commandName,
        @JsonProperty("command_data") Map<String, Object> commandData) {

    public <T> T commandData2(Class<T> commandDataClass) {

        final ObjectMapper mapper = new ObjectMapper();
//        TypeFactory.defaultInstance().construct
        T data = mapper.convertValue(commandData, commandDataClass);
        return data;
    }

}
