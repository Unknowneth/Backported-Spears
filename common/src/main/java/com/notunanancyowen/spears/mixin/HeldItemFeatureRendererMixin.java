package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.components.KineticWeapon;
import com.notunanancyowen.spears.components.SwingAnimation;
import com.notunanancyowen.spears.dataholders.SpearUser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererMixin {
    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void addSpearAnimation(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if(entity.handSwingProgress > 0F && entity.getMainArm() == arm && stack.get(Spears.SWING_ANIMATION) instanceof SwingAnimation s && s.swingType().equals("stab")) {
            float f = stack.get(Spears.KINETIC_WEAPON) instanceof KineticWeapon k ? k.forwardMovement() : 0F;
            float g = 0.125F;
            float h = entity.isUsingItem() ? 1F : entity.getHandSwingProgress(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true));
            float i = MathHelper.clamp(MathHelper.getLerpProgress(h, 0.05F, 0.2F), 0.0F, 1.0F);
            i *= i;
            float j = MathHelper.clamp(MathHelper.getLerpProgress(h, 0.4F, 1.0F), 0.0F, 1.0F);
            if(j < 0.5F) j = j == 0.0F ? 0.0F : (float)(Math.pow(2.0F, (double)20.0F * (double) j - (double)10.0F) / (double)2.0F);
            else j = j == 1.0F ? 1.0F : (float)(((double)2.0F - Math.pow(2.0F, (double)-20.0F * (double) j + (double)10.0F)) / (double)2.0F);
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(70.0F * (i - j)), 0.0F, -g, g);
            matrices.translate(0.0F, f * (i - j), 0.0F);
        }
        if(entity.getActiveItem().equals(stack) && stack.get(Spears.KINETIC_WEAPON) instanceof KineticWeapon k) {
            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
            float f = stack.getMaxUseTime(entity) - (entity.getItemUseTimeLeft() - tickDelta + 1.0F);
            if(f == 0F || !entity.isUsingItem()) return;
            SpearsClient.holdUpAnimation lv = SpearsClient.holdUpAnimation.play(k, f);
            int i = arm == Arm.RIGHT ? 1 : -1;
            float g = 1.0F - lv.raiseProgress() - 1.0F;
            g = 1.0F - (1.0F + 2.70158F * g * g * g + 1.70158F * MathHelper.square(g));
            float j = 0F;
            if(entity instanceof SpearUser s) j = s.getTimeSinceLastKineticAttack(tickDelta);
            j = (1.0F - MathHelper.square(MathHelper.square(1.0F - MathHelper.clamp(MathHelper.getLerpProgress(j, 1.0F, 3.0F), 0.0F, 1.0F)))) + (MathHelper.cos((float) Math.PI * MathHelper.clamp(MathHelper.getLerpProgress(j, 3.0F, 10.0F), 0.0F, 1.0F)) - 1.0F) / 2.0F;
            j *= 0.4F;
            if(j >= 10.0F) j = 0F;
            matrices.translate(0.0, -j * 0.4, (-k.forwardMovement() * (g - lv.raiseBackProgress()) + j));
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(lv.raiseProgress() * 70.0F - lv.raiseBackProgress() * 70.0F), 0.0F, -0.03125F, 0.125F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lv.raiseProgress() * i * 90.0F - lv.swayProgress() * i * 90.0F), 0.0F, 0.0F, 0.125F);
        }
    }
}
