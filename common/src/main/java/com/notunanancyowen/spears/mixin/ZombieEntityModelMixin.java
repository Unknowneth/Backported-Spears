package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.components.SwingAnimation;
import net.minecraft.client.render.entity.model.AbstractZombieModel;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieModel.class)
public abstract class ZombieEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/entity/mob/HostileEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/CrossbowPosing;meleeAttack(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;ZFF)V"), cancellable = true)
    private void isUsingASpear(HostileEntity hostileEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if(hostileEntity.getMainHandStack().get(Spears.SWING_ANIMATION) instanceof SwingAnimation s && s.swingType().equals("stab")) ci.cancel();
    }
}
