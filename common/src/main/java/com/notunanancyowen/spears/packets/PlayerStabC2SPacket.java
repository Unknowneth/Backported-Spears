package com.notunanancyowen.spears.packets;

import com.notunanancyowen.spears.Spears;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record PlayerStabC2SPacket(int entityId) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, PlayerStabC2SPacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT,
            PlayerStabC2SPacket::entityId,
            PlayerStabC2SPacket::new
    );
    @Nullable public Entity getEntity(World world) {
        return world.getEntityById(this.entityId);
    }
    @Override public Id<? extends CustomPayload> getId() {
        return ID;
    }
    public static final Id<PlayerStabC2SPacket> ID = new Id<>(Identifier.of(Spears.MOD_ID, "send_stab_attack_to_server"));
}
