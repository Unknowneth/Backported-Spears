package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.components.AttackRange;
import com.notunanancyowen.spears.components.PiercingWeapon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Inject(method = "isInAttackRange", at = @At("HEAD"), cancellable = true)
    private void canAttackWithSpear(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        MobEntity me = (MobEntity)(Object)this;
        if(me.getMainHandStack().get(Spears.ATTACK_RANGE) instanceof AttackRange(float minReach, float maxReach) && me.getMainHandStack().get(Spears.PIERCING_WEAPON) instanceof PiercingWeapon p) {
            double d = me.distanceTo(entity);
            cir.setReturnValue(d >= minReach && d <= maxReach && me.getRotationVector(me.getPitch(), me.getBodyYaw()).dotProduct(entity.getPos().subtract(me.getPos()).normalize()) > p.hitboxMargin()) ;
        }
    }
    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    private void attackWithSpear(Entity target, CallbackInfoReturnable<Boolean> cir) {
        MobEntity me = (MobEntity)(Object)this;
        if(me.getMainHandStack().get(Spears.PIERCING_WEAPON) instanceof PiercingWeapon p) {
            me.lookAtEntity(target, 30F, 30F);
            me.setBodyYaw(me.getHeadYaw());
            cir.setReturnValue(p.stab(me, EquipmentSlot.MAINHAND));
        }
    }
}
