package com.notunanancyowen.spears.fabric.packets;

import com.notunanancyowen.spears.Spears;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record PlayerStabC2SPacket(int entityId) implements FabricPacket {
    public PlayerStabC2SPacket(PacketByteBuf buf) {
        this(buf.readInt());
    }
    @Override public void write(PacketByteBuf buf) {
        buf.writeInt(entityId());
    }
    @Nullable public Entity getEntity(World world) {
        return world.getEntityById(this.entityId);
    }
    @Override public PacketType<?> getType() {
        return TYPE;
    }
    public static final PacketType<PlayerStabC2SPacket> TYPE = PacketType.create(new Identifier(Spears.MOD_ID, "send_stab_attack_to_server"), PlayerStabC2SPacket::new);
}
