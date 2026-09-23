package com.notunanancyowen.spears.forge;

import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.forge.packets.PlayerStabC2SPacket;
import com.notunanancyowen.spears.forge.packets.SyncLungeC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(Identifier.of(Spears.MOD_ID, "send_stab_attack_to_server")).clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true).networkProtocolVersion(() -> "1").simpleChannel();
    public static void register() {
        INSTANCE.<PlayerStabC2SPacket>messageBuilder(PlayerStabC2SPacket.class, NetworkDirection.PLAY_TO_SERVER.ordinal()).encoder(PlayerStabC2SPacket::encode).decoder(PlayerStabC2SPacket::new).consumerMainThread(PlayerStabC2SPacket::handle).add();
        if(ModList.get().isLoaded("bettercombat")) INSTANCE.<SyncLungeC2SPacket>messageBuilder(SyncLungeC2SPacket.class, NetworkDirection.PLAY_TO_SERVER.ordinal()).encoder(SyncLungeC2SPacket::encode).decoder(SyncLungeC2SPacket::new).consumerMainThread(SyncLungeC2SPacket::handle).add();
    }
    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }
}
