package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
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
    @Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER), cancellable = true)
    private void spearAttack(CallbackInfoReturnable<Boolean> cir) {
        if(player != null && player.getMainHandStack().getItem() instanceof SpearItem) if(player.getAttackCooldownProgress(0F) < 1F) cir.setReturnValue(false);
        else {
            if(SpearsClient.syncSpears != null) SpearsClient.syncSpears.apply(player.getId());
            else player.sendMessage(Text.of("Missing packet for spears:send_stab_attack_to_server"));
            player.swingHand(Hand.MAIN_HAND);
            player.resetLastAttackedTicks();
            cir.setReturnValue(true);
        }
    }
}
