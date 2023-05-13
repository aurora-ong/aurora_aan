package ong.aurora.aan.core.network.message;

import ong.aurora.aan.core.network.AANNetworkMessage;

public record BlockchainBalancerBlockRequestMessage(Long blockchainIndex) implements AANNetworkMessage {
}
