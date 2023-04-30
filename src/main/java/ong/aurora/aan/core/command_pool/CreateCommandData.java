package ong.aurora.aan.core.command_pool;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record CreateCommandData(@JsonProperty("command_name") String commandName,
                                @JsonProperty("command_data") Map<String, Object> commandData
) {
}