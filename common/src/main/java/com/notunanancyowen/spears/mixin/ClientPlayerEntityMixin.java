package com.notunanancyowen.spears.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {
    @Shadow public Input input;
    @Shadow @Final protected MinecraftClient client;
    @Shadow private boolean usingItem;
    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", shift = At.Shift.AFTER))
    private void removeMovementSpeedPenalty(CallbackInfo ci) {
        if(getActiveItem() != null && getActiveItem().getItem() instanceof SpearItem) {
            input.movementForward *= 5;
            input.movementSideways *= 5;
        }
    }
    @WrapMethod(method = "canStartSprinting")
    private boolean makeSurePlayerCanSprint(Operation<Boolean> original) {
        if(getActiveItem() != null && getActiveItem().getItem() instanceof SpearItem) {
            usingItem = false;
            var result = original.call();
            usingItem = true;
            return result;
        }
        return original.call();
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void triggerOnSwingEffects(CallbackInfo ci) {
        if(Spears.hasBetterCombat && client.getNetworkHandler() != null) try {
            if(SpearsClient.getUpswingTicks == null) SpearsClient.getUpswingTicks = MinecraftClient.class.getDeclaredMethod("getUpswingTicks");
            if(SpearsClient.getUpswingTicks.invoke(client) instanceof Integer i) {
                if(i > SpearsClient.lastUpswingTicksForBetterCombat) SpearsClient.syncSpearsWithBetterCombat.apply(getId());
                SpearsClient.lastUpswingTicksForBetterCombat = i;
            }
        }
        catch (Throwable ignore) {
        }
    }
}
