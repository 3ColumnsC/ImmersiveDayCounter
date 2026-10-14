package com.threecolumnsstudio.immersivedaycounter.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class IDCNetwork {

    private static Sender sender;

    private IDCNetwork() {
    }

    public static void setSender(Sender sender) {
        IDCNetwork.sender = sender;
    }

    public static void sendToPlayer(ServerPlayer player, DayPayload payload) {
        if (sender == null) {
            return;
        }
        sender.sendToPlayer(player, payload);
    }

    public static void sendToAll(MinecraftServer server, DayPayload payload) {
        if (sender == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sender.sendToPlayer(player, payload);
        }
    }

    public interface Sender {
        void sendToPlayer(ServerPlayer player, DayPayload payload);
    }
}
