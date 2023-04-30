package ong.aurora.aan.core.command_pool;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.aan.command.Command;

public record CommandIntent(@JsonProperty("command_name") Command commandName,
                            @JsonProperty("command_status") CommandStatus commandStatus
) {
}