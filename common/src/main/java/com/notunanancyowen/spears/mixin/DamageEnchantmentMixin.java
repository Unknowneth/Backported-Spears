package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageEnchantment.class)
public abstract class DamageEnchantmentMixin {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    private void allowSpears(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(stack.getItem() instanceof AxeItem || stack.getItem() instanceof SpearItem) cir.setReturnValue(true);
        else cir.setReturnValue(EnchantmentTarget.WEAPON.isAcceptableItem(stack.getItem()));
    }
}
