package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.components.KineticWeapon;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "usageTick", at = @At("HEAD"), cancellable = true)
    private void tickSpearRightClick(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(world.isClient()) return;
        ItemStack me = (ItemStack)(Object)this;
        if(me.get(Spears.KINETIC_WEAPON) instanceof KineticWeapon k) {
            k.usageTick(me, remainingUseTicks, user, user.getActiveHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            ci.cancel();
        }
    }
}
