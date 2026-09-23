package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;
    @Shadow protected abstract ModelPart getArm(Arm arm);
    @Shadow public BipedEntityModel.ArmPose rightArmPose;
    @Shadow public BipedEntityModel.ArmPose leftArmPose;
    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void rightArmSpear(T entity, CallbackInfo ci) {
        if(!rightArmPose.isTwoHanded() || handSwingProgress > 0F) if(handleSpearAnimationPerArm(entity, Arm.RIGHT) && !entity.isUsingItem() && handSwingProgress > 0F) ci.cancel();
    }
    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void leftArmSpear(T entity, CallbackInfo ci) {
        if(!leftArmPose.isTwoHanded() || handSwingProgress > 0F) if(handleSpearAnimationPerArm(entity, Arm.LEFT) && !entity.isUsingItem() && handSwingProgress > 0F) ci.cancel();
    }
    @Unique private boolean handleSpearAnimationPerArm(T entity, Arm arm) {
        ItemStack itemStack = (entity.getMainArm() == arm ? entity.getMainHandStack() : entity.getOffHandStack());
        if(!(itemStack.getItem() instanceof SpearItem) && !(itemStack.isIn(Spears.SPEARS) && handSwingProgress <= 0F)) return false;
        var usedArm = getArm(arm);
        usedArm.yaw = -0.1F * head.yaw;
        usedArm.pitch = (-(float)Math.PI / 2F) + head.pitch + 0.8F;
        if(entity.isFallFlying() || entity.getLeaningPitch(1F) > 0.0F) usedArm.pitch -= 0.9599311F;
        if(entity.isUsingItem() && entity.getActiveItem().getItem() instanceof SpearItem spear) {
            int i = arm == Arm.RIGHT ? 1 : -1;
            SpearsClient.holdUpAnimation lv = SpearsClient.holdUpAnimation.play(spear, entity.getItemUseTime() + MinecraftClient.getInstance().getTickDelta());
            usedArm.yaw += -i * lv.swayScaleFast() * (float) (Math.PI / 180.0) * lv.swayIntensity();
            usedArm.roll += -i * lv.swayScaleSlow() * (float) (Math.PI / 180.0) * lv.swayIntensity() * 0.5F;
            usedArm.pitch += (float) (Math.PI / 180.0) * (-40.0F * lv.raiseProgressStart() + 30.0F * lv.raiseProgressMiddle() + 20.0F * lv.raiseProgressEnd() - 20.0F * lv.lowerProgress() + 10.0F * lv.raiseBackProgress() + 0.6F * lv.swayScaleSlow() * lv.swayIntensity());
        }
        return true;
    }
    @Inject(method = "animateArms", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sin(F)F", ordinal = 3), cancellable = true)
    private void addSpearAnimation(T entity, float animationProgress, CallbackInfo ci) {
        if(entity.getMainHandStack().getItem() instanceof SpearItem) {
            float f = entity.isUsingItem() ? 1F : handSwingProgress;
            Arm arm = entity.getMainArm();
            rightArm.yaw -= body.yaw;
            leftArm.yaw -= body.yaw;
            leftArm.pitch -= body.yaw;
            float g = -(MathHelper.cos(((float)Math.PI * MathHelper.clamp(MathHelper.getLerpProgress(f, 0.0F, 0.05F), 0.0F, 1.0F))) - 1.0F) / 2.0F;
            float h = MathHelper.clamp(MathHelper.getLerpProgress(f, 0.05F, 0.2F), 0.0F, 1.0F);
            h *= h;
            float i = MathHelper.clamp(MathHelper.getLerpProgress(f, 0.4F, 1.0F), 0.0F, 1.0F);
            if(i < 0.5F) i = i == 0.0F ? 0.0F : (float)(Math.pow(2.0F, (double)20.0F * (double)i - (double)10.0F) / (double)2.0F);
            else i = i == 1.0F ? 1.0F : (float)(((double)2.0F - Math.pow(2.0F, (double)-20.0F * (double)i + (double)10.0F)) / (double)2.0F);
            getArm(arm).pitch += (90.0F * g - 120.0F * h + 30.0F * i) * ((float)Math.PI / 180F);
            float trueYaw = head.yaw;
            while(trueYaw >= Math.PI * 2) trueYaw -= (float)Math.PI * 2F;
            while(trueYaw <= Math.PI * -2) trueYaw += (float)Math.PI * 2F;
            getArm(arm).yaw += trueYaw * (h - i);
            ci.cancel();
        }
    }
    @Inject(method = "animateArms", at = @At("HEAD"), cancellable = true)
    private void animateLanceAttack(T entity, float animationProgress, CallbackInfo ci) {
        if((entity.isUsingItem() ? entity.getActiveItem() : entity.getMainHandStack()).getItem() instanceof SpearItem spear && entity.isUsingItem()) {
            float f = entity.getItemUseTime() + MinecraftClient.getInstance().getTickDelta();
            float g = spear.delayTicks() == 0 ? 1F : spear.delayTicks();
            if(f > spear.getUseTicks()) {
                f = (1F - (f - spear.getUseTicks())) / g;
                if(f < 0F) f = 0F;
            }
            else {
                f /= g;
                if(f > 1F) f = 1F;
            }
            var usedArm = getArm(entity.getActiveHand() == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite());
            float trueYaw = head.yaw;
            while(trueYaw >= Math.PI * 2) trueYaw -= (float)Math.PI * 2F;
            while(trueYaw <= Math.PI * -2) trueYaw += (float)Math.PI * 2F;
            usedArm.yaw = (float)MathHelper.clamp(trueYaw, -Math.PI / 2.4, Math.PI / 2.4) * f;
            usedArm.pitch += head.pitch * 0.5F * f - (float)(Math.PI / 180.0 * (entity.isFallFlying() || entity.getLeaningPitch(1F) > 0.0F ? 55.0 : 30.0)) * f;
            ci.cancel();
        }
    }
}
