package com.notunanancyowen.spears.dataholders;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;

public interface SpearUser {
    float getTimeSinceLastKineticAttack(float tickProgress);
    boolean isInPiercingCooldown(Entity target, int cooldownTicks);
    void startPiercingCooldown(Entity target);
    int countSpearedMobs();
    boolean pierce(EquipmentSlot slot, Entity target, float damage, boolean dealDamage, boolean knockback, boolean dismount);
}
