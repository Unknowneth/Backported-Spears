package com.notunanancyowen.spears.fabric.client;

import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.fabric.packets.PlayerStabC2SPacket;
import com.notunanancyowen.spears.fabric.packets.SyncLungeC2SPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class SpearsFabricClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        SpearsClient.syncSpears = i -> {
            ClientPlayNetworking.send(new PlayerStabC2SPacket(i));
            return true;
        };
        SpearsClient.syncSpearsWithBetterCombat = i -> {
            ClientPlayNetworking.send(new SyncLungeC2SPacket(i));
            return true;
        };
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }
}
