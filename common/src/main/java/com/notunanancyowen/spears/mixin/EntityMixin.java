package com.notunanancyowen.spears.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.notunanancyowen.spears.dataholders.MovementFixer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements MovementFixer {
    @Shadow public double prevX;
    @Shadow public double prevY;
    @Shadow public double prevZ;
    @SuppressWarnings("all")
    @Override public Vec3d getMovement() {
        Entity me = (Entity & MovementFixer)(Object)this;
        return me.getControllingPassenger() instanceof PlayerEntity playerEntity && playerEntity instanceof MovementFixer m && me.isAlive() ? m.getMovement() : me.getPos().add(me.getVelocity()).subtract(prevX, prevY, prevZ);
    }
    @Inject(method = "setOnGround(ZLnet/minecraft/util/math/Vec3d;)V", at = @At("TAIL"))
    private void setMovement(boolean onGround, Vec3d movement, CallbackInfo ci) {
        Entity me = (Entity & MovementFixer)(Object)this;
        if(me instanceof ServerPlayerEntity s && s instanceof MovementFixer m) m.setMovement(movement);
    }
    @WrapMethod(method = "move")
    private void forceAddVelocity(MovementType movementType, Vec3d movement, Operation<Void> original) {
        Entity me = (Entity & MovementFixer)(Object)this;
        var oldPos = me.getPos();
        original.call(movementType, movement);
        if(me.getControllingPassenger() instanceof ServerPlayerEntity s && s instanceof MovementFixer m) m.setMovement(me.getPos().subtract(oldPos));
    }
}
