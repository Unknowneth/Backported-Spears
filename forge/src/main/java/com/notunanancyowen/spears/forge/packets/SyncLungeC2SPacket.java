package com.notunanancyowen.spears.forge.packets;

import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncLungeC2SPacket {
    private final int playerId;
    public SyncLungeC2SPacket(int playerId) {
        this.playerId = playerId;
    }
    public SyncLungeC2SPacket(PacketByteBuf buffer) {
        this(buffer.readInt());
    }
    public void encode(PacketByteBuf buffer) {
        buffer.writeInt(playerId);
    }
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity sp = context.get().getSender();
            if(sp != null && sp.getId() == playerId && sp.getMainHandStack().getItem() instanceof SpearItem) {
                ItemStack stack = sp.getMainHandStack();
                int lunge = stack.getEnchantmentLevel(Spears.LUNGE);
                if(!sp.isFallFlying() && !sp.isTouchingWater() && sp.getHungerManager().getFoodLevel() > 5 && lunge > 0) {
                    Vec3d vec3d2 = SpearItem.transformLocalPos(SpearItem.getYawAndPitch(sp.getRotationVector()), new Vec3d(0, 0, 1)).multiply(new Vec3d(1, 0, 1)).multiply(lunge * 0.458);
                    sp.addVelocity(vec3d2);
                    sp.velocityModified = true;
                    sp.velocityDirty = true;
                    if(sp.getWorld() instanceof ServerWorld server) server.playSound(null, sp.getX(), sp.getY(), sp.getZ(), Spears.SPEAR_LUNGE, sp.getSoundCategory(), 1F, 1F);
                    if(!sp.isCreative()) sp.addExhaustion(lunge * 4F);
                    stack.damage(1, sp, (user) -> user.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
