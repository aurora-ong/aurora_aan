package ong.aurora.ann.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import ong.aurora.commons.command.Command;

import java.util.Map;

public record CommandIntent(@JsonProperty("command_name") Command commandName,
                            @JsonProperty("command_status") CommandStatus commandStatus
) {
}