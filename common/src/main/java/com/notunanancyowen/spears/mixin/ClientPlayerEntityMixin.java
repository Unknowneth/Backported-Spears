package com.notunanancyowen.spears.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.components.UseEffects;
import com.notunanancyowen.spears.packets.TriggerStabEffectsC2SPacket;
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
    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", shift = At.Shift.AFTER))
    private void removeMovementSpeedPenalty(CallbackInfo ci) {
        if(getActiveItem() != null && getActiveItem().getComponents().get(Spears.USE_EFFECTS) instanceof UseEffects u) {
            float f = 5F * u.strafeSpeed();
            input.movementForward *= f;
            input.movementSideways *= f;
        }
    }
    @ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean makeSurePlayerCanSprint(boolean original) {
        if(getActiveItem() != null && getActiveItem().getComponents().get(Spears.USE_EFFECTS) instanceof UseEffects u && u.allowSprinting()) return false;
        return original;
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void triggerOnSwingEffects(CallbackInfo ci) {
        if(Spears.hasBetterCombat && client.getNetworkHandler() != null) try {
            if(SpearsClient.getUpswingTicks == null) SpearsClient.getUpswingTicks = MinecraftClient.class.getDeclaredMethod("getUpswingTicks");
            if(SpearsClient.getUpswingTicks.invoke(client) instanceof Integer i) {
                if(i > SpearsClient.lastUpswingTicksForBetterCombat) SpearsClient.sendPacketUniversal.apply(new TriggerStabEffectsC2SPacket(true));
                SpearsClient.lastUpswingTicksForBetterCombat = i;
            }
        }
        catch (Throwable ignore) {
        }
    }
}
