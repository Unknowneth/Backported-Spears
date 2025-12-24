package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.components.PiercingWeapon;
import com.notunanancyowen.spears.packets.PlayerStabC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Nullable public abstract ClientPlayNetworkHandler getNetworkHandler();
    @Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER), cancellable = true)
    private void spearAttack(CallbackInfoReturnable<Boolean> cir) {
        if(player != null) if(player.getMainHandStack().get(Spears.MINIMUM_ATTACK_CHARGE) instanceof Float f && player.getAttackCooldownProgress(0F) < f) cir.setReturnValue(false);
        else if(player.getMainHandStack().get(Spears.PIERCING_WEAPON) instanceof PiercingWeapon p) {
            p.playSound(player);
            if(getNetworkHandler() != null) SpearsClient.sendPacketUniversal.apply(new PlayerStabC2SPacket(player.getId())); //getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new PlayerStabC2SPacket(player.getId())));
            player.swingHand(Hand.MAIN_HAND);
            player.resetLastAttackedTicks();
            cir.setReturnValue(true);
        }
    }
}
