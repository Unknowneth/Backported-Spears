package com.notunanancyowen.spears.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.components.SwingAnimation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinEntityModel.class)
public abstract class PiglinEntityModelMixin<T extends MobEntity> extends PlayerEntityModel<T> {
    public PiglinEntityModelMixin(ModelPart root, boolean thinArms) {
        super(root, thinArms);
    }
    @Inject(method = "rotateMainArm", at = @At("HEAD"), cancellable = true)
    private void stabWithSpear(T entity, CallbackInfo ci) {
        if(entity.getMainHandStack().get(Spears.SWING_ANIMATION) instanceof SwingAnimation s && s.swingType().equals("stab")) ci.cancel();
    }
    @WrapOperation(method = "setAngles(Lnet/minecraft/entity/mob/MobEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/CrossbowPosing;meleeAttack(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;ZFF)V"))
    private void stabWithSpear1(ModelPart leftArm, ModelPart rightArm, boolean attacking, float swingProgress, float animationProgress, Operation<Void> original, @Local(argsOnly = true) T entity) {
        if(entity.getMainHandStack().get(Spears.SWING_ANIMATION) instanceof SwingAnimation s && s.swingType().equals("stab")) return;
        original.call(leftArm, rightArm, attacking, swingProgress, animationProgress);
    }
    @Inject(method = "animateArms(Lnet/minecraft/entity/mob/MobEntity;F)V", at = @At("HEAD"), cancellable = true)
    private void stabWithSpear2(T mobEntity, float f, CallbackInfo ci) {
        if(mobEntity.getMainHandStack().get(Spears.SWING_ANIMATION) instanceof SwingAnimation s && s.swingType().equals("stab")) {
            super.animateArms(mobEntity, f);
            ci.cancel();
        }
    }
}
