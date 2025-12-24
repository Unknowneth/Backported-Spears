package com.notunanancyowen.spears.fabric.client;

import com.notunanancyowen.spears.SpearsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class SpearsFabricClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        SpearsClient.sendPacketUniversal = (payload) -> {
            ClientPlayNetworking.send(payload);
            return true;
        };
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }
}
