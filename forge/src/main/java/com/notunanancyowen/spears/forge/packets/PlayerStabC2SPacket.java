package com.notunanancyowen.spears.forge.packets;

import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerStabC2SPacket {
    private final int playerId;
    public PlayerStabC2SPacket(int playerId) {
        this.playerId = playerId;
    }
    public PlayerStabC2SPacket(PacketByteBuf buffer) {
        this(buffer.readInt());
    }
    public void encode(PacketByteBuf buffer) {
        buffer.writeInt(playerId);
    }
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity sp = context.get().getSender();
            if(sp != null && sp.getId() == playerId) {
                if(sp.getMainHandStack().getItem() instanceof SpearItem s) s.stab(sp, EquipmentSlot.MAINHAND);
                sp.resetLastAttackedTicks();
            }
        });
        context.get().setPacketHandled(true);
    }
}
