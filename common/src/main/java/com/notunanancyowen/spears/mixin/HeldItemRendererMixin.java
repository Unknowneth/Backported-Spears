package com.notunanancyowen.spears.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.components.KineticWeapon;
import com.notunanancyowen.spears.components.SwingAnimation;
import com.notunanancyowen.spears.dataholders.SpearUser;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @WrapOperation(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 12))
    private void suppressNormalSwing(MatrixStack instance, float x, float y, float z, Operation<Void> original, @Local(argsOnly = true) ItemStack itemStack) {
        if(itemStack.get(Spears.SWING_ANIMATION) instanceof SwingAnimation s && (s.swingType().equals("stab") || s.swingType().equals("none"))) return;
        original.call(instance, x, y, z);
    }
    @WrapOperation(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 8))
    private void suppressSwapAnimation(HeldItemRenderer instance, MatrixStack matrices, Arm arm, float equipProgress, Operation<Void> original, @Local(argsOnly = true) AbstractClientPlayerEntity player, @Local(argsOnly = true) ItemStack itemStack) {
        if(itemStack.get(Spears.SWING_ANIMATION) instanceof SwingAnimation s && s.swingType().equals("stab") && player.handSwingProgress > 0F) original.call(instance, matrices, arm, 0F);
        else original.call(instance, matrices, arm, equipProgress);
    }
    @WrapOperation(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applySwingOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V"))
    private void spearAnimation(HeldItemRenderer instance, MatrixStack matrices, Arm arm, float swingProgress, Operation<Void> original, @Local(argsOnly = true) ItemStack itemStack) {
        if(itemStack.get(Spears.SWING_ANIMATION) instanceof SwingAnimation s) {
            if(s.swingType().equals("stab")) {
                float g = -(MathHelper.cos(((float)Math.PI * MathHelper.clamp(MathHelper.getLerpProgress(swingProgress, 0.0F, 0.05F), 0.0F, 1.0F))) - 1.0F) / 2.0F;
                float h = MathHelper.clamp(MathHelper.getLerpProgress(swingProgress, 0.05F, 0.2F), 0.0F, 1.0F);
                h *= h;
                float j = MathHelper.clamp(MathHelper.getLerpProgress(swingProgress, 0.4F, 1.0F), 0.0F, 1.0F);
                if(j < 0.5F) j = j == 0.0F ? 0.0F : (float)(Math.pow(2.0F, (double)20.0F * (double) j - (double)10.0F) / (double)2.0F);
                else j = j == 1.0F ? 1.0F : (float)(((double)2.0F - Math.pow(2.0F, (double)-20.0F * (double) j + (double)10.0F)) / (double)2.0F);
                boolean trident = itemStack.isOf(Items.TRIDENT);
                matrices.translate(j * 0.1F * (g - h), -0.075F * (g - j), (trident ? -0.4F * (g - j) : 0F) + 0.65F * (g - h));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-(trident ? 50.0F : 70.0F) * (g - j)));
                matrices.translate(0.0F, 0.0F, (double)-0.25F * (double)(j - h));
                return;
            }
            if(s.swingType().equals("none")) return;
        }
        original.call(instance, matrices, arm, swingProgress);
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;"))
    private void insertSpearAnimation(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, @Share("suppress") LocalBooleanRef suppress) {
        if(player.getActiveHand() == hand && item.get(Spears.KINETIC_WEAPON) instanceof KineticWeapon k) {
            Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            int i = arm == Arm.RIGHT ? 1 : -1;
            matrices.translate(i * 0.56F, -0.52F, -0.72F);
            SpearsClient.holdUpAnimation lv = SpearsClient.holdUpAnimation.play(k, item.getMaxUseTime(player) - (player.getItemUseTimeLeft() - tickDelta + 1.0F));
            matrices.translate(
                    (i * (lv.raiseProgress() * 0.15F + lv.raiseProgressEnd() * -0.05F + lv.swayProgress() * -0.1F + lv.swayScaleSlow() * 0.005F)),
                    (lv.raiseProgress() * -0.075F + lv.raiseProgressMiddle() * 0.075F + lv.swayScaleFast() * 0.01F),
                    lv.raiseProgressStart() * 0.05 + lv.raiseProgressEnd() * -0.05 + lv.swayScaleSlow() * 0.005F
            );
            float f = lv.raiseProgress();
            if (f < 0.5F) f = 4.0F * f * f * (7.189819F * f - 2.5949094F) / 2.0F;
            else {
                float g = 2.0F * f - 2.0F;
                f = (g * g * (3.5949094F * g + 2.5949094F) + 2.0F) / 2.0F;
            }
            matrices.multiply(
                    RotationAxis.POSITIVE_X
                            .rotationDegrees(
                                    -65.0F * f - 35.0F * (1F - lv.lowerProgress()) + 100.0F * lv.raiseBackProgress() + -0.5F * lv.swayScaleFast()
                            ),
                    0.0F,
                    0.1F,
                    0.0F
            );
            matrices.multiply(
                    RotationAxis.NEGATIVE_Y
                            .rotationDegrees(
                                    i * (-90.0F * MathHelper.clamp(MathHelper.getLerpProgress(lv.raiseProgress(), 0.5F, 0.55F), 0.0F, 1.0F) + 90.0F * lv.swayProgress() + 2.0F * lv.swayScaleSlow())
                            ),
                    i * 0.15F,
                    0.0F,
                    0.0F
            );
            float h = 0F;
            if(player instanceof SpearUser s) h = s.getTimeSinceLastKineticAttack(tickDelta);
            h = (1.0F - MathHelper.square(MathHelper.square(1.0F - MathHelper.clamp(MathHelper.getLerpProgress(h, 1.0F, 3.0F), 0.0F, 1.0F)))) + (MathHelper.cos((float) Math.PI * MathHelper.clamp(MathHelper.getLerpProgress(h, 3.0F, 10.0F), 0.0F, 1.0F)) - 1.0F) / 2.0F;
            h *= 0.4F;
            if(h >= 10.0F) h = 0F;
            matrices.translate(0F, -h, 0.0F);
            suppress.set(true);
        }
    }
    @WrapOperation(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2))
    private void suppressEquipOffsetForUseAnimation(HeldItemRenderer instance, MatrixStack matrices, Arm arm, float equipProgress, Operation<Void> original, @Share("suppress") LocalBooleanRef suppress) {
        if(!suppress.get()) original.call(instance, matrices, arm, equipProgress);
    }
}
