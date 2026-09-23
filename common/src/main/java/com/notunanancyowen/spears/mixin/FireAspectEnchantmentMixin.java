package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.FireAspectEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireAspectEnchantment.class)
public class FireAspectEnchantmentMixin extends Enchantment {
    protected FireAspectEnchantmentMixin(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes) {
        super(weight, target, slotTypes);
    }
    @Override public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof SpearItem || super.isAcceptableItem(stack);
    }
}
