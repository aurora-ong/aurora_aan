package ong.aurora.aan.core.network.message;

import ong.aurora.aan.core.network.AANNetworkMessage;
import ong.aurora.aan.event.Event;

public record BlockchainBalancerBlockResponseMessage(Event event) implements AANNetworkMessage {
}
