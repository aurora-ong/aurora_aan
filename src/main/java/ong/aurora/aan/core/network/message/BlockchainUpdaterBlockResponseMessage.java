package ong.aurora.aan.core.network.message;

import ong.aurora.aan.core.network.AANNetworkMessage;

public record BlockchainUpdaterBlockResponseMessage(Long blockchainIndex) implements AANNetworkMessage {
}
