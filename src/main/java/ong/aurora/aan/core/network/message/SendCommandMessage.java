package ong.aurora.aan.core.network.message;

import ong.aurora.aan.command.Command;
import ong.aurora.aan.core.network.AANNetworkMessage;

public record SendCommandMessage(Command command) implements AANNetworkMessage {
}
