package com.notunanancyowen.spears.enchantments;

import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class Lunge extends Enchantment {
    public Lunge() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }
    @Override public int getMaxLevel() {
        return 3;
    }
    @Override public int getMinPower(int level) {
        return 5 + level * 8;
    }
    @Override public int getMaxPower(int level) {
        return getMinPower(level) + 20;
    }
    @Override public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof SpearItem;
    }
}
