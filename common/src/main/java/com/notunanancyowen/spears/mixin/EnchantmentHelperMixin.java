package com.notunanancyowen.spears.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.notunanancyowen.spears.enchantments.Lunge;
import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.enchantment.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @WrapOperation(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z"), require = 0)
    private static boolean doNotAcceptFabric(EnchantmentTarget instance, Item item, Operation<Boolean> original, @Local Enchantment enchantment) {
        if(enchantment instanceof Lunge) return enchantment.isAcceptableItem(item.getDefaultStack());
        if(instance == EnchantmentTarget.WEAPON && (enchantment instanceof DamageEnchantment || enchantment instanceof KnockbackEnchantment || enchantment instanceof FireAspectEnchantment) && item instanceof SpearItem) return true;
        return original.call(instance, item);
    }
    @SuppressWarnings("all")
    @WrapOperation(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;canApplyAtEnchantingTable(Lnet/minecraft/item/ItemStack;)Z"), require = 0)
    private static boolean doNotAcceptForge(Enchantment instance, ItemStack stack, Operation<Boolean> original) {
        if(instance instanceof Lunge) return instance.isAcceptableItem(stack);
        if(instance.target == EnchantmentTarget.WEAPON && (instance instanceof DamageEnchantment || instance instanceof KnockbackEnchantment || instance instanceof FireAspectEnchantment) && stack.getItem() instanceof SpearItem) return true;
        return original.call(instance, stack);
    }
}
