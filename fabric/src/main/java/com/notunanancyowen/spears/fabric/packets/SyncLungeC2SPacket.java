package com.notunanancyowen.spears.fabric.packets;

import com.notunanancyowen.spears.Spears;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record SyncLungeC2SPacket(int entityId) implements FabricPacket {
    public SyncLungeC2SPacket(PacketByteBuf buf) {
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
    public static final PacketType<SyncLungeC2SPacket> TYPE = PacketType.create(new Identifier(Spears.MOD_ID, "sync_lunge_enchantment_to_server"), SyncLungeC2SPacket::new);
}
