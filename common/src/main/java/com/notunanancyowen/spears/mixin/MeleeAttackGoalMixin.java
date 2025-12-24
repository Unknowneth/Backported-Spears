package com.notunanancyowen.spears.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.components.AttackRange;
import com.notunanancyowen.spears.components.SwingAnimation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {
    @Shadow private int cooldown;
    @Shadow @Final protected PathAwareEntity mob;
    @Inject(method = "resetCooldown", at = @At("TAIL"))
    private void longerAttackCooldownForSpears(CallbackInfo ci) {
        if(mob.getMainHandStack().get(Spears.PIERCING_WEAPON) != null) mob.setBodyYaw(mob.getHeadYaw());
        if(mob.getMainHandStack().get(Spears.SWING_ANIMATION) instanceof SwingAnimation s) cooldown += Math.max(s.swingTicks() - 6, 0);
    }
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/pathing/EntityNavigation;startMovingTo(Lnet/minecraft/entity/Entity;D)Z"))
    private boolean applyMinimumAttackDistanceForSpears(EntityNavigation instance, Entity entity, double speed, Operation<Boolean> original) {
        if(mob.getMainHandStack().get(Spears.ATTACK_RANGE) instanceof AttackRange a) {
            var path = instance.findPathTo(entity, (int)a.minReach());
            return path != null && instance.startMovingAlong(path, speed);
        }
        return original.call(instance, entity, speed);
    }
}
